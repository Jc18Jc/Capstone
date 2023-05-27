package com.example.myapplication.Chat

import android.R.bool
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.DTO.history
import com.example.myapplication.DTO.taxiList
import com.example.myapplication.MainActivity
import com.example.myapplication.MainScreen.DetailRoomActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityChatroomBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class ChatroomActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatroomBinding
    private var userName: String = ""
    private var roomName: String = ""
    private var hostName: String = ""
    private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스
    private var roomCondition = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatroomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        userName = intent.getStringExtra("userName").toString()
        roomName = intent.getStringExtra("attend_roomName").toString()
        Log.d("login_chatRoom_room", roomName)
        Log.d("login_chatRoom_user", userName)

        binding.noConstraintLayout.visibility = View.VISIBLE

        /** 아님 이렇게 안하고 만약 방 참여하기만 해도 활성화되는걸로 할까..? */
         if (roomName != "null") {
            binding.noConstraintLayout.visibility = View.INVISIBLE
            val bundle = Bundle()
            bundle.putString("attend_roomName", roomName)
            bundle.putString("userName", userName)
            replaceFragment(bundle)
        } else {
            binding.noConstraintLayout.visibility = View.VISIBLE
        }

        hostName = loadRoomData(roomName)

        /** 뒤로가기 버튼*/
        binding.acBtnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("attend_roomName", roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        var chargeOnclick:Boolean = false


        /** 정산하기 버튼 */
        binding.btnCharge.setOnClickListener {
            var hostName = ""
            // CalculatorDialog 두 번째 파라미터 자리에 destination 값 넣어주세요
            db.collection("Room")
                .document(roomName)
                .get()
                .addOnSuccessListener { documentSnapshot  ->
                    if (documentSnapshot.exists()) {
                        hostName = documentSnapshot.getString("host").toString()
                        Log.d("ChatRoomActivity_btnCharge", "$hostName")


                        var people = ""
                        db.collection("Room")
                            .document(roomName)
                            .collection("member")
                            .get()
                            .addOnSuccessListener {
                                people = it.size().toString()

                                val dialog = CalculationDialog(this, "$roomName", "$people", "$hostName")
                                Log.d("ChatRoom", "host name is $hostName")
                                dialog.show()
                            }

                        /** 이거 바꿔야함 ************************************8 !!!!!!!!@@@@@@@@@@@@@@@@@@@@ */


                    } else {
                        Log.d("ChatRoomActivity", "document is not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("ChatFragment", "$e")
                }
        }

        var roomCost = ""
        /** 방 나가기 버튼 (호스트 이름이랑 userName이 같으면 방 삭제함. 아님 삭제 안됨 */
        binding.acBtnOut.setOnClickListener {
            db.collection("Chat")
                .document(roomName)
                .collection("Cost")
                .document("cost")
                .get()
                .addOnSuccessListener { chatdocumentSnapshot ->
                    roomCost = chatdocumentSnapshot.getString("cost").toString()
                }

            db.collection("Room")
                .document(roomName)
                .addSnapshotListener { querySnapshot, e ->
                    if (e != null) {
                        Log.d("firebase_error", "failed $e!")
                        return@addSnapshotListener
                    }
                    if (querySnapshot != null && querySnapshot.exists()) {
                        // 방에 저장된 비용


                        // 비용 불러오기
                        db.collection("Chat")
                            .document(roomName)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("chatRoom", "delete_success")
                                Toast.makeText(this, "채팅방이 종료되었습니다.", Toast.LENGTH_SHORT).show()


                                // 날짜를 저장하기 위한 함수
                                val current = LocalDateTime.now()
                                val year = current.year.toString()
                                val month = current.monthValue.toString()
                                val day = current.dayOfMonth.toString()
                                // 날짜를 문자열로 저장
                                val date = year + "년 " + month + "월 " + day + "일"


                                // history에 저장
                                db.collection("Room")
                                    .document(roomName)
                                    .collection("member")
                                    .get()
                                    .addOnSuccessListener {
                                        val history = history(roomCost, date, roomName)
                                        // member에 있는 user들을 document에 담아서 각 history에 저장
                                        for (document in it) {
                                            db.collection("User")
                                                .document(document.id)
                                                .collection("history")
                                                .document(roomName)
                                                .set(history)
                                                .addOnSuccessListener {
                                                    Log.d("member_name", "${document.id}")
                                                    Log.d("history_set", "successed set history")
                                                }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.d("history_set", "failed set history")
                                    }

                                // 방 정보 지우기기
                                db.collection("Room")
                                    .document(roomName)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("room_delete", "successed delete room")
                                    }
                                    .addOnFailureListener {
                                        Log.d("room_delete", "failed delete room")
                                    }

                                // 참여하기가 끝나면 메인화면으로 이동시켜줌
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra(
                                    "attend_roomName",
                                    "null"
                                )    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
                                intent.putExtra("userName", userName)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Log.d("chatRoom", "$e")
                            }

                    }
                    // 쿼리 스넵샷이 null값인 경우
                    else {
                        finish()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("attend_roomName", "null")    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                    }
                }
        }

        binding.btnVisibleBack.setOnClickListener {
            // 참여하기가 끝나면 메인화면으로 이동시켜줌
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("attend_roomName", "null")    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
            intent.putExtra("userName", userName)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        // Return to MainActivity and set flag for returning from ChatActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("isReturnedFromChatActivity", true)
        intent.putExtra("attend_roomName", roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
        intent.putExtra("userName", userName)
        startActivity(intent)
        finish()
    }

    // ChatFragment로 프래그먼트 교체 (LoginFragment에서 호출할 예정)
    fun replaceFragment(bundle: Bundle) {
        val destination = ChatFragment()
        destination.arguments = bundle      // 닉네임을 받아옴
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_frame, destination)
            .commit()
    }

    private fun loadRoomData(inputDestination: String) : String {
        var returnHostName = ""
        db.collection("Room")
            .document(inputDestination)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("firebase_error", "failed $e!")
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.exists()) {
                    val destination = querySnapshot.getString("destination") ?: ""
                    val endTime = querySnapshot.getString("endTime") ?: ""
                    val maxPeople = querySnapshot.getString("maxPeople") ?: ""
                    returnHostName = querySnapshot.getString("host") ?: ""

                    var calc_people = ""
                    db.collection("Room")
                        .document(destination)
                        .collection("member")
                        .get()
                        .addOnSuccessListener {
                            calc_people = it.size().toString()

                            binding.meetPlaceText.text = destination
                            binding.meetTimeText.text = endTime
                            binding.peopleText.text = calc_people + "명 / " + maxPeople + "명"
                            binding.textDestinationTitle.text = destination
                        }
                }
            }
        return returnHostName
    }

    private fun checkRoomStatus(inputDestination: String) {
        db.collection("Room")
            .document("$inputDestination")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("firebase_error", "failed $e!")
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.exists()) {
                    val roomOpen = querySnapshot.getBoolean("ChatRoom") ?: false
                    Log.d("room_checkRoomSta", roomOpen.toString())
                    updateRoomStatus(roomOpen)
                }
            }
    }
    private fun updateRoomStatus(roomOpen: Boolean) {
        if (roomOpen) {
            binding.noConstraintLayout.visibility = View.INVISIBLE

            val bundle = Bundle()
            bundle.putString("attend_roomName", roomName)
            bundle.putString("userName", userName)
            replaceFragment(bundle)
        } else {
            binding.noConstraintLayout.visibility = View.VISIBLE
        }
    }
}