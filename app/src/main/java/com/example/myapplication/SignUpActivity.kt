package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.example.myapplication.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.backButton.setOnClickListener {
            startActivity(Intent(this,LogInActivity::class.java))
        }

        binding.signUpButton.setOnClickListener {
            var name = binding.nameTextView.text
            var id = binding.idTextView.text
            var password1 = binding.passwordTextView1.text
            var password2 = binding.passwordTextView1.text
            /*
            // 회원 가입이 성공하려면 중복검사 통과 후, 비밀번호 1과 비밀번호 2가 일치하며, 이름 칸이 비어있으면 안된다.
            if (checkedId && password1 == password2 && name!=null) {
                val data = User(name.toString(), id.toString(), password1.toString())
                database.child("user").setValue(data)
                Toast.makeText(this, "회원가입이 완료되었습다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,LogInActivity::class.java))
            }
            // 이름 칸이 비어있을 경우
            else if(binding.nameTextView.text==null) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            // 비밀번호 1과 비밀번호 2가 일치하지 않을 경우우
           else if(binding.passwordTextView1.text != binding.passwordTextView2.text) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }*/
            auth?.createUserWithEmailAndPassword(id.toString(), password1.toString())?.addOnCompleteListener { task->
                //정상적으로 이메일과 비번이 전달되어
                //새 유저 계정을 생성과 서버db 저장 완료 및 로그인
                //즉, 기존에 있는 계정이 아니다!
                if(task.isSuccessful) {
                    Toast.makeText(this, "회원가입이 완료되었습다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,LogInActivity::class.java))
                }
                else if (task.exception?.message.isNullOrEmpty()==false){
                    //예외메세지가 있다면 출력
                    //에러가 났다거나 서버가 연결이 실패했다거나
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
                else{
                    //여기가 실행되는 경우는 이미 db에 해당 이메일과 패스워드가 있는 경우

                }
            }
        }
    }
}