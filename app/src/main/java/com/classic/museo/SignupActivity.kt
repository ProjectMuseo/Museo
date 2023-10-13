package com.classic.museo

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.classic.museo.databinding.ActivitySignupBinding
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupEditPw2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.btnSignup2.setTextColor(Color.WHITE)
                binding.btnSignup2.isEnabled = false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSignup2.setTextColor(Color.WHITE)
                binding.btnSignup2.isEnabled = false
            }

            override fun afterTextChanged(editable: Editable?) {
                val pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,16}$"
                val pw1 = binding.signupEditPw.text.toString()
                val pw2 = editable.toString()
                val pattern = Pattern.compile(pwPattern)
                val matcher = pattern.matcher(pw1)

                if(matcher.matches()){
                    binding.passwardAnswer.text = "적합한 비밀번호 입니다."
                    binding.passwardAnswer.setTextColor(Color.parseColor("#026A31"))
                }
                else {
                    binding.passwardAnswer.text = "비밀번호를 확인하여주세요."
                    binding.passwardAnswer.setTextColor(Color.RED)
                }

                if(pw1 == pw2 && matcher.matches()){
                    binding.passwardAnswer2.text = "비밀번호가 일치합니다."
                    binding.passwardAnswer2.setTextColor(Color.parseColor("#026A31"))
                    binding.btnSignup2.setTextColor(Color.BLACK)
                    binding.btnSignup2.isEnabled = true
                }
                else {
                    binding.passwardAnswer2.text = "비밀번호를 확인해 주세요."
                    binding.passwardAnswer2.setTextColor(Color.RED)
                    binding.btnSignup2.setTextColor(Color.WHITE)
                    binding.btnSignup2.isEnabled = false
                }
            }
        })
    }
}