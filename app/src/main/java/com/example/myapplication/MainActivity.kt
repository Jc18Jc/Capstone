package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.myapplication.Chat.ChatroomActivity
import com.example.myapplication.History.HistoryFragment
import com.example.myapplication.MainScreen.CreateActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.main.MainListFragment

private lateinit var userName: String
private lateinit var attend_roomName : String

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding


    private val fl: FrameLayout by lazy {
        findViewById(R.id.layout)
    }

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check Return From ChatActivity
        val isReturnedFromChatActivity = intent.getBooleanExtra("isReturnedFromChatActivity", false)
        val isReturnedFromCreateActivity = intent.getBooleanExtra("isReturnedFromCreateActivity", false)
        if (isReturnedFromChatActivity) {
            binding.bottomNavigationView.selectedItemId = R.id.menu_home
        }
        if (isReturnedFromCreateActivity) {
            binding.bottomNavigationView.selectedItemId = R.id.menu_home
        }


      /*  val isRoomEnterSuccess = intent.getBooleanExtra("enterRoom_success", false)
        val isRoomEnterSuccess_nowPeople = intent.getStringExtra("enterRoom_success_nowPeople").toString()
        if (isRoomEnterSuccess) {
            attend_roomName = intent.getStringExtra("attend_roomName").toString()

            FirebaseFirestore.getInstance() .collection("Room").document(attend_roomName)
                .update("nowPeople", (isRoomEnterSuccess_nowPeople.toInt() + 1).toString())
        }*/

/*
        *//** detailRoomActivity에서 성공적으로 참여하기를 누르게 된다면 해당 페이지로 roomName값이 넘어오게 된다. *//*
        attend_roomName = intent.getStringExtra("attend_roomName").toString()
        Log.d("login_attend_roomName", attend_roomName)*/

        replaceFragment(MainListFragment())

        /** LoginActivity에서 로그인된 유저에 대한 Name을 가져온다. */


        /** bottom navigation view에 대한 clickListener */
        binding.bottomNavigationView.selectedItemId = R.id.menu_home    // 초기 아이콘을 main으로 설정해둔다
        binding.bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_history -> replaceFragment(HistoryFragment())
                    R.id.menu_user -> replaceFragment(UserFragment())
                    R.id.make_room -> {
                        attend_roomName = intent.getStringExtra("attend_roomName").toString()
                        val intent = Intent(this, CreateActivity::class.java)
                        Log.d("login_attend", attend_roomName)
                        intent.putExtra("attend_roomName", attend_roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                    }
                    R.id.chat_room -> {
                        attend_roomName = intent.getStringExtra("attend_roomName").toString()
                        val intent = Intent(this, ChatroomActivity::class.java)
                        Log.d("login_attend", attend_roomName)
                        intent.putExtra("attend_roomName", attend_roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                    }
                    R.id.menu_home -> replaceFragment(MainListFragment())
                }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()

        userName = intent.getStringExtra("userName").toString()
        Log.d("login_success_main", userName)

        // detailRoomActivity에서 성공적으로 참여하기를 누르게 된다면 해당 페이지로 roomName값이 넘어오게 된다.
        attend_roomName = intent.getStringExtra("attend_roomName").toString()
        Log.d("login_attend_roomName", attend_roomName)

        bundle.putString("key_loginRoom", attend_roomName)

        bundle.putString("key_userName", userName)
        Log.d("login_success_mainActivity", userName)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction().replace(fl.id, fragment).commit()
    }
}