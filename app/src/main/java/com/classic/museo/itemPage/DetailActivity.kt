package com.classic.museo.itemPage

//import com.kakao.vectormap.MapView
import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.classic.museo.R
import com.classic.museo.data.Record
import com.classic.museo.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.regex.Pattern


class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding
    private var db = Firebase.firestore
    private var auth: FirebaseAuth? = null
    private var subId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //뒤로가기 버튼
        binding.dtBack.setOnClickListener {
            finish()
        }

        //공유버튼
        share()

        //카카오 맵
        kakaoMap()

        //공공데이터 받아오기
        museoInfo()
        //즐겨찾기 버튼
        likeBtn()

    }

    //카카오 맵
    fun kakaoMap() {
        val mapView = MapView(this)
        binding.kakaoMap.addView(mapView)
        val a = intent.getParcelableExtra<Record>("museumData")!!


        //위도 데이터
        val Latitude = a.latitude.toDouble()
        //경도 데이터
        val longitude = a.longitude.toDouble()
        //카카오맵 위치 나타내기
        val mapPoint = MapPoint.mapPointWithGeoCoord(Latitude, longitude)


        // 확대 레벨 설정 (값이 작을수록 더 확대됨)
        mapView.setMapCenterPoint(mapPoint, true)
        mapView.setZoomLevel(1, true)

        //마커 생성
        val marker = MapPOIItem()

        marker.itemName = "이곳은 ${a.fcltyNm} 입니다"
        marker.mapPoint = mapPoint
        marker.markerType = MapPOIItem.MarkerType.RedPin
        marker.selectedMarkerType = MapPOIItem.MarkerType.BluePin
        mapView.addPOIItem(marker)
    }

    //공유버튼 기능
    private fun share() {
        val a = intent.getParcelableExtra<Record>("museumData")!!
        binding.dtShare.setOnClickListener {

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    a.homepageUrl
                )
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }
    }

    //공공api데이터 가져오기
    fun museoInfo() {
        val a = intent.getParcelableExtra<Record>("museumData")!!
        //시설명
        binding.dtTitle.text = a.fcltyNm
        //운영기관전화번호
        binding.dtNumber.text = a.operPhoneNumber
        //소재지 도로명 주소
        binding.dtAddress.text = a.rdnmadr
        //박물관 미술관 소개
        binding.dtIntroduction.text = a.fcltyIntrcn
        //휴관정보
        binding.closeDayTx.text = a.rstdeInfo
        //이용시간
        binding.hoursuseTx.text =
            "${a.weekdayOperOpenHhmm}~${a.weekdayOperColseHhmm} (공휴일 ${a.holidayOperOpenHhmm} ~ ${a.holidayCloseOpenHhmm})"
        //관람료 기타정보(입장료)
        binding.moneyTx.text = "${a.adultChrge}원 ${a.etcChrgeInfo}"
        //운영홈페이지
        binding.homepageTx.text = a.homepageUrl
        //관리기관명
        binding.organizationTx.text = a.institutionNm
        //박물관 구분
        binding.SortationTx.text = a.fcltyType

        //제목 클릭 시 해당 홈페이지로 이동
        val dt_title = findViewById<View>(R.id.dt_title) as TextView
        val text = a.fcltyNm

        dt_title.text = text

        val transform = Linkify.TransformFilter() { _, _ ->
            ""
        }
        val pattern = Pattern.compile(a.fcltyNm)    //링크 걸 단어를 맞게 설정해 줘야함
        Linkify.addLinks(dt_title, pattern, a.homepageUrl, null, transform)
    }

    //즐겨찾기 버튼
    fun likeBtn() {
        binding.dtLike.setOnClickListener {

            val a = intent.getParcelableExtra<Record>("museumData")!!   //박물관 정보
            val title = binding.dtTitle.text.toString()
            val address = binding.dtAddress.text.toString()
            val category = a.fcltyType
            var museum = ""
            if (binding.dtTitle.text.endsWith("미술관")) {
                museum = "미술관"
            } else {
                museum = "박물관"
            }

            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()

            // 현재 로그인한 사용자의 UID 가져오기
            val currentUser = auth.currentUser
            val uid = currentUser?.uid

            db.collection("museoInfo")
                .whereEqualTo("fcltyNm", a.fcltyNm)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {

                        //firestor 박물관별 문서id 가져오기
                        subId = document.id
                        Log.e("asd", subId)


                        db.collection("users")
                            .document("$uid")
                            .collection("myLike")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    val subcollectionId = document.id
                                    Log.e("dd", subcollectionId)

                                    val subcollectionRef = db.collection("users")
                                        .document("$uid")
                                        .collection("myLike")
                                    Log.e("asd", subId)

                                    // 중복을 확인할 문서 ID가 이미 존재하는지 검사
                                    subcollectionRef.whereEqualTo(FieldPath.documentId(), subId)
                                        .get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val querySnapshot = task.result

                                                if (querySnapshot != null) {
                                                    if (!querySnapshot.isEmpty) {
                                                        // 중복되는 문서 ID가 존재할 때
                                                        Log.e("성공맞냐", "${subId}")

                                                        //데이터 삭제
                                                        val collectionPath =
                                                            "users/$uid/myLike"
                                                        subcollectionRef.whereEqualTo(
                                                            FieldPath.documentId(),
                                                            subId
                                                        )
                                                        db.collection(collectionPath)
                                                            .document(subId)
                                                            .delete()
                                                            .addOnSuccessListener {
                                                                Log.d(
                                                                    "성공",
                                                                    "DocumentSnapshot successfully deleted!"
                                                                )
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.w(
                                                                    "실패",
                                                                    "Error deleting document",
                                                                    e
                                                                )
                                                            }

                                                    } else {
                                                        // 중복되는 문서 ID가 존재하지 않을 때
                                                        Log.e("실패맞냐", "${subId}")

                                                        //데이터 생성
                                                        if (uid != null) {
                                                            val collectionPath =
                                                                "users/$uid/myLike"
                                                            // 서브컬렉션에 새 문서 추가
                                                            val data = hashMapOf(
                                                                "title" to title,
                                                                "address" to address,
                                                                "category" to category,
                                                                "museum" to museum,
                                                            )
                                                            db.collection(collectionPath)
                                                                .document(subId)
                                                                .set(data)
                                                                .addOnSuccessListener { documentReference ->
                                                                    Log.e(
                                                                        "성공zzz",
                                                                        "${documentReference}"
                                                                    )
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.e("실패eee", "$e")
                                                                }
                                                        } else {
                                                            Toast.makeText(
                                                                this,
                                                                "사용자가 로그인하지 않았습니다..",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                // 쿼리를 실행하는 동안 오류가 발생했을 때
                                                println("쿼리를 실행하는 동안 오류가 발생했습니다: ${task.exception}")
                                            }
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("qwe", "Error getting documents: ", exception)
                            }

                    }
                }
        }
    }
}