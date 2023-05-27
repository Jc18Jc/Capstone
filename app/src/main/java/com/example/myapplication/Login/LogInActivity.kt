package com.example.myapplication.Login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.History.HistoryFragment
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivityLogInBinding
import com.google.api.ResourceDescriptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    var userName = ""
    private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.signUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.logInBtn.setOnClickListener {
            val email = binding.idInputEditText.text.toString()
            val password=binding.pwInputEditText.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this,"로그인 되었습니다.",Toast.LENGTH_SHORT).show()

                        // 로그인된 정보 중에서 해당하는 user의 정보를 찾아서 닉네임을 불러와야 한다.
                        // 저장된 email을 바탕으로 name을 가져와봅세
                        Log.d("login", auth.currentUser?.email.toString())
                        var userEmail = auth.currentUser?.email.toString()
                        //val data:Any?
                        db.collection("User").document("$userEmail")
                            .get().addOnSuccessListener {documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val data = documentSnapshot.get("name")
                                    if (data != null) {
                                        // 데이터가 존재하면 사용 가능
                                        // 원하는 작업 수행
                                        Log.d("login_success", data.toString())

                                        /** user의 name정보를 data에 저장했다.
                                         * 이 데이터를 먼저 mainActivity에 전송해 줄 것이다.*/
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.putExtra("userName", data.toString())

                                        val bundle = Bundle()
                                        bundle.putString("key_userName", data.toString())
                                        Log.d("login_success_mainActivity", data.toString())
                                        val historyFragment = HistoryFragment()
                                        historyFragment.arguments = bundle

                                        startActivity(intent)
                                    } else {
                                        // 데이터가 null인 경우 처리
                                        Log.d("login_fail", "data is null")
                                    }
                                } else {
                                    // 문서가 존재하지 않는 경우 처리
                                    Log.d("login_success", "document is not exist")
                                }
                            }
                            .addOnFailureListener { e ->
                                // 불러오기 실패 시 처리
                                Log.d("login_success", "$e")
                            }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}