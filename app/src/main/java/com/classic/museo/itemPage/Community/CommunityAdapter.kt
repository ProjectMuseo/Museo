package com.classic.museo.itemPage.Community

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.classic.museo.data.CommunityDTO
import com.classic.museo.databinding.CommunityImageBinding
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CommunityAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var review = mutableListOf<CommunityDTO>()
    val db = Firebase.firestore

//    init {
//        uid = FirebaseAuth.getInstance().uid
//        firestore = FirebaseFirestore.getInstance()
//
//        firestore?.collection(uid!!)?.orderBy("post", Query.Direction.DESCENDING)
//            ?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
//                review.clear()
//                if (querySnapshot == null) return@addSnapshotListener
//
//                for (snapshot in querySnapshot!!.documents){
//                    var item = snapshot.toObject(CommunityDTO::class.java)
//                    review.add(item!!)
//                }
//                notifyDataSetChanged()
//            }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            CommunityImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount() = review.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentReview = review[position]
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val holder = holder as ImageViewHolder

        holder.title.text = currentReview.title
        holder.nickname.text = currentReview.NickName
        holder.userID.text = currentReview.UserId
        holder.date.text = currentReview.date
        holder.museum.text = currentReview.museum
        Log.d("holder", "sj $currentReview")

    }

    fun postFirestore() {

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("post").get()
            .addOnSuccessListener { result ->
                Log.d("postFirestore", "sj postFirestore : $result")

                val newPostData = mutableListOf<CommunityDTO>()
                for (i in result) {
                    if (i.exists()) {
                        Log.d("postFirestore", "sj postFirestore : ${newPostData.size} , $newPostData")
                        val postData = i.toObject(CommunityDTO::class.java)
                        newPostData.add(postData)
                    }
                }
                review.clear()
                review.addAll(newPostData)
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("postFirestore", "error : $e")
            }
    }

    fun usersFirestore() {
        val userFirestore = FirebaseFirestore.getInstance()
        userFirestore.collection("users").get()
            .addOnSuccessListener { result ->
                Log.d("usersFirestore", "sj usersFirestore : $result")

                val newData = mutableListOf<CommunityDTO>()
                for (i in result) {
                    if (i.exists()) {
                        Log.d("usersFirestore", "sj usersFirestore : ${newData.size} , $newData")
                        val userstData = i.toObject(CommunityDTO::class.java)
                        for(userData in review){
                            Log.d("usersFirestore","$userstData, $userData")
                            if(userstData.UID == userData.UID){
                                userData.UserId = userstData.UserId
                                userData.NickName = userstData.NickName
                                newData.add(userData)
                            }
                        }
                    }
                }
                review.clear()
                review.addAll(newData)
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("usersFirestore", "error : $e")
            }
    }



inner class ImageViewHolder(val binding: CommunityImageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    var title: TextView = binding.textCommunityTitle
    var nickname: TextView = binding.communityNickname
    var userID: TextView = binding.communityId
    var date: TextView = binding.dateCommunityImage
    var museum: TextView = binding.communityMuseumName

}
}