package com.example.myapplication.Chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentChatBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment: Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!


    private var currentUser: String? = ""            // 현재 닉네임
    private var roomName: String? = ""


    private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스
    private lateinit var registration: ListenerRegistration    // 문서 수신
    private val chatList = arrayListOf<ChatLayout>()    // 리사이클러 뷰 목록
    private lateinit var adapter: ChatAdapter   // 리사이클러 뷰 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // LoginFragment 에서 입력한 닉네임을 가져옴
        arguments?.let {
            currentUser = it.getString("userName").toString()
            Log.d("login_chatRoomFragment", currentUser.toString())
            roomName = it.getString("attend_roomName").toString()
            Log.d("login_chatRoomFragment", roomName.toString())
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // currentUser 변수 사용 가능
        // 리사이클러 뷰 설정
        binding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ChatAdapter(currentUser.toString(), chatList)
        binding.rvList.adapter = adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root


        // Toast.makeText(context, "현재 닉네임은 ${currentUser}입니다.", Toast.LENGTH_SHORT).show()


        // 리사이클러 뷰 설정
        /*binding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ChatAdapter(currentUser, chatList)
        binding.rvList.adapter = adapter*/

        /** host값 알아와서 Chat에 저장해준다. */
        var host = ""
        db.collection("Room")
            .document(roomName.toString())
            .get()
            .addOnSuccessListener { documentSnapshot  ->
                if (documentSnapshot.exists()) {
                    host = documentSnapshot.getString("host").toString()
                    if (host != null) {
                        // "host" 필드 값 사용
                        Log.d("ChatFragment", "host is $host")
                        val hostInfo = hashMapOf(
                            "host" to host
                        )
                        db.collection("Chat")?.document(roomName.toString())?.set(hostInfo)
                    } else {
                        Log.d("ChatFragment", "host is null String")
                    }
                } else {
                    Log.d("ChatFragment", "document is not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.d("ChatFragment", "$e")
            }

        // 채팅창이 공백일 경우 버튼 비활성화
        binding.etChatting.addTextChangedListener { text ->
            binding.btnSend.isEnabled = text.toString() != ""
        }

        // 입력 버튼
        binding.btnSend.setOnClickListener {
            // 입력 데이터
            val data = hashMapOf(
                "nickname" to currentUser,
                "contents" to binding.etChatting.text.toString(),
                "time" to Timestamp.now()
            )
            if (data != null) {
                db.collection("Chat")
                    .document(roomName.toString())
                    .collection("ChatList")
                    .add(data)
                    .addOnSuccessListener {
                        binding.etChatting.text.clear()
                        Log.w("ChatFragment", "Document added: $it")
                    }
                    .addOnFailureListener { e ->
                        Log.w("ChatFragment", "Error occurs: $e")
                    }
            } else {
                Log.w("ChatFragment", "Data is null. Cannot save to Firestore.")
            }
        }
        super.onViewCreated(view, savedInstanceState)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //chatList.add(ChatLayout("알림", "$currentUser 닉네임으로 입장했습니다.", ""))
        val enterTime = Date(System.currentTimeMillis())


        val item = ChatLayout("관리자", "택시가 도착하면, 오른쪽 상단의 정산하기 버튼을 눌러주세요.", "", 1)
        chatList.add(item)

        val nowTime = Timestamp.now()
        val sf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
        sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val time = sf.format(nowTime.toDate())
        roomName = arguments?.getString("attend_roomName")
        Log.d("login_fuck", roomName.toString())

        registration = db.collection("Chat")
            .document(roomName.toString())
            .collection("ChatList")
            .orderBy("time", Query.Direction.ASCENDING)
            //.limit(1)
            .addSnapshotListener { snapshots, e ->
                // 오류 발생 시
                if (e != null) {
                    Log.w("ChatFragment", "Listen failed: $e")
                    return@addSnapshotListener
                }

                // 원하지 않는 문서 무시
                if (snapshots!!.metadata.isFromCache)
                    return@addSnapshotListener

                // 문서 수신
                for (doc in snapshots!!.documentChanges) {
                    val timestamp = doc.document["time"] as Timestamp

                    // 문서가 추가될 경우 리사이클러 뷰에 추가
                    if (doc.type == DocumentChange.Type.ADDED) {
                        val nickname = doc.document["nickname"].toString()
                        val contents = doc.document["contents"].toString()

                        // 타임스탬프를 한국 시간, 문자열로 바꿈
                        val sf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
                        sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                        val time = sf.format(timestamp.toDate())

                        if (currentUser == nickname) {
                            val item = ChatLayout(nickname, contents, time, 2)
                            chatList.add(item)
                        }
                        else {
                            val item = ChatLayout(nickname, contents, time, 1)
                            chatList.add(item)
                        }

                        // 채팅방에 추가한다.
                        //chatList.add(item)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        registration.remove()
        _binding = null
        chatList.clear()
    }
}