package com.example.myapplication.MainScreen

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.Chat.ChatroomActivity
import com.example.myapplication.DTO.Room
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.databinding.ActivityDetailRoomBinding
import com.example.myapplication.databinding.ActivityRoomStateBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore

private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스

class DetailRoomActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDetailRoomBinding

    private lateinit var mMap: GoogleMap
    var decisionJoin = false
    var fbFirestore : FirebaseFirestore? = null

    private lateinit var destination: String
    private lateinit var endTime:String
    private lateinit var maxPeople:String
    private lateinit var nowPeople:String
    private lateinit var userName:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_info_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
        binding.joinButton.setOnClickListener {
            decisionJoin = true
            finish()
        }

        /** MainActivity에서 UserName을 전달받음 */
        userName = intent.getStringExtra("userName").toString()
        Log.d("detailRoom", userName.toString())

        // = intent.getStringExtra("key_destination")!!
        /** mainList에서 화면에 띄워줄 데이터들을 전달받음 */

        destination = intent.getStringExtra("key_destination").toString()
        endTime = intent.getStringExtra("key_endTime").toString()
        maxPeople = intent.getStringExtra("key_maxPeople").toString()
        nowPeople = intent?.getStringExtra("key_nowPeople").toString()
        userName = intent?.getStringExtra("key_userName").toString()



        Log.d("itemClicked_detailRoom", "$destination,$endTime,$maxPeople,$userName")
        Log.d("login_success_main", userName.toString())

        binding.routeText.text = destination
        binding.endTimeText.text = endTime
        binding.maxPeopleText.text = nowPeople + "명 / " + maxPeople + "명"

        /** 뒤로가기 버튼 액션 */
        binding.drbtnBack.setOnClickListener {
            finish()
        }

        /** 참여하기 버튼 */
        binding.joinButton.setOnClickListener {
            // 해당하는 방의 파베에 이름을 올리고 종료
            updateRoomData(destination.toString(), userName.toString())
        }
    }

    private fun updateRoomData(inputDestination: String, userName: String) {
        db.collection("Room")
            .document(inputDestination)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val maxPeople = documentSnapshot.getString("maxPeople") ?: ""
                    // val nowPeople = documentSnapshot.getString("nowPeople") ?: "" // 저것도 length값으로 처리


                    // 최대 인원에 도달한 경우 처리
                    if (nowPeople.toInt() >= maxPeople.toInt()) {
                        Log.d("firebase_error", "Reached maximum number of people in the room")
                        Toast.makeText(this, "방 인원이 가득 찼습니다.", Toast.LENGTH_SHORT).show()
                        finish() // 방 인원이 가득 찼으므로 액티비티를 종료합니다.
                        return@addOnSuccessListener
                    }

                    db.collection("Room")
                        .document(inputDestination)
                        .collection("member")
                        .document(userName)
                        .get()
                        .addOnSuccessListener { memberSnapshot ->
                            if (memberSnapshot != null && memberSnapshot.exists()) {
                                // 이미 동일한 userName으로 문서가 존재하는 경우 처리
                                Log.d("firebase_error", "User with the same name already exists")
                                Toast.makeText(this, "이미 방에 참여하셨습니다.", Toast.LENGTH_SHORT).show()
                                finish() // 이미 참여한 경우 액티비티를 종료합니다.
                            } else {
                                /** 맴버 추가해주는 부분의 코드 */
                                val memberData = hashMapOf("name$nowPeople" to userName)
                                db.collection("Room")
                                    .document(inputDestination)
                                    .collection("member")
                                    .document(userName)
                                    .set(memberData)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Data saved successfully")
                                        Toast.makeText(this, "방에 참여하셨습니다.", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this, ChatroomActivity::class.java)
                                        intent.putExtra("attend_roomName", inputDestination)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
                                        intent.putExtra("userName", userName)
                                        startActivity(intent)
                                        // 참여가 완료되었으므로 액티비티를 종료합니다.
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error saving data", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error checking member document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error retrieving room document", e)
            }
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var startLatitude: Double
        var startLongitude: Double
        var destinationLatitude: Double
        var destinationLongitude: Double
        // 파이어베이스에서 destination과 일치하는 것을

       destination = intent.getStringExtra("key_destination")!!
        val ref = db.collection("Room")?.document(destination)
        ref?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot.exists()) {
                    startLatitude = documentSnapshot.getString("startLat")!!.toDouble()
                    startLongitude = documentSnapshot.getString("startLng")!!.toDouble()
                    destinationLatitude = documentSnapshot.getString("destinationLat")!!.toDouble()
                    destinationLongitude = documentSnapshot.getString("destinationLng")!!.toDouble()

                    val startMarker = LatLng(startLatitude, startLongitude)
                    val destinationMarker = LatLng(destinationLatitude, destinationLongitude)

                    // 시작 카메라 위치, 시작 마커와 도착 마커의 가운데
                    val initLat = (startLatitude+destinationLatitude)/2
                    val initLng = (startLongitude+destinationLongitude)/2
                    val initLatLng = LatLng(initLat, initLng)

                    Log.d("detailRoom", "$initLat")

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initLatLng, 14F))
                    mMap.addMarker(MarkerOptions().position(startMarker).title("출발지")).showInfoWindow()
                    mMap.addMarker(MarkerOptions().position(destinationMarker).title("도착지"))
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        val bundle = Bundle()
        bundle.putBoolean("decision",decisionJoin)
    }
}