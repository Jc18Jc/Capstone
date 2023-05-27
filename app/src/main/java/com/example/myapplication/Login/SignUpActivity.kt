package com.example.myapplication.Login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.DTO.user
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스
    var fbAuth : FirebaseAuth? = null
    var fbFirestore : FirebaseFirestore? = null

    var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        fbAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()

        var name = binding.nameTextView.text
        var id = ""
        var password1 = ""
        var password2 = ""

        /** backButton action 다사 구현해야 할 수도 */
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.signUpButton.setOnClickListener {
            id = binding.idTextView.text.toString()
            password1 = binding.passwordTextView1.text.toString()
            password2 = binding.passwordTextView2.text.toString()

            auth?.createUserWithEmailAndPassword(id, password1)?.addOnCompleteListener { task->
                // 비밀번호와 비밀번호 재입력이 일치하지 않는다
                if(!password1.equals(password2)) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                }

                //정상적으로 이메일과 비번이 전달되어
                //새 유저 계정을 생성과 서버db 저장 완료 및 로그인
                //즉, 기존에 있는 계정이 아니다
                else if(task.isSuccessful) {
                    Toast.makeText(this, "회원가입이 완료되었습다.", Toast.LENGTH_SHORT).show()

                    /** 회원가입 된 정보를 firestore에 저장*/
                    var userInfo = user(id, name.toString(), fbAuth?.uid.toString())
                    fbFirestore?.collection("User")?.document(id.toString())?.set(userInfo)

                    Log.d("login_signup", auth.currentUser?.email.toString())
                    var userEmail = auth.currentUser?.email.toString()

                    // 저장된 email을 바탕으로 name을 가져와봅세
                    db.collection("User").document("$userEmail")
                        .get().addOnSuccessListener {documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val data = documentSnapshot.data
                                if (data != null) {
                                    // 데이터가 존재하면 사용 가능
                                    // 원하는 작업 수행
                                    Log.d("signUp_success", data.toString())
                                } else {
                                    // 데이터가 null인 경우 처리
                                    Log.d("signUp_fail", "data is null")
                                }
                            } else {
                                // 문서가 존재하지 않는 경우 처리
                                Log.d("signUp_success", "document is not exist")
                            }
                        }
                        .addOnFailureListener { e ->
                            // 불러오기 실패 시 처리
                            Log.d("signUp_success", "$e")

                        }





                    finish()
                }
                else if (task.exception?.message.isNullOrEmpty()==false) {
                    //예외메세지가 있다면 출력
                    //에러가 났다거나 서버가 연결이 실패했다거나
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
                else{
                    //여기가 실행되는 경우는 이미 db에 해당 이메일과 패스워드가 있는 경우
                    Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}