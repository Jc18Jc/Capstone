package com.example.myapplication.MainScreen

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.DTO.Room
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import kotlin.properties.Delegates


class CreateActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityCreateBinding
    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var startLatLng: LatLng    // 출발지 위도 경도
    private lateinit var destinationLatLng: LatLng  // 도착지 위도 경도
    private var toggle by Delegates.notNull<Boolean>()    // toggle이 true면 출발지, false면 도착지에 주소 입력
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    private var userName:String = ""
    private var roomName:String = ""

    //private GoogleMap mMap;
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        var auth: FirebaseAuth? = null
        var firestore: FirebaseFirestore? = null

        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference

        /**  userName의 값을 받아옴 */
        userName = intent.getStringExtra("userName").toString()
        roomName = intent.getStringExtra("attend_roomName").toString()

        Log.d("login_crA_roomName", roomName)

        ArrayAdapter.createFromResource(this, R.array.number, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.headCountSpinner.adapter = adapter
            }

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toggle=true

        // 텍스트뷰를 클릭해서 포커스를 주면 해당 텍스트뷰의 백그라운드를 바꾸고 다른 텍스트뷰의 백그라운드를 없앰
        binding.inputStartTextView.setOnClickListener {
            binding.inputStartTextView.background = getDrawable(R.drawable.textview_focus_design)
            binding.inputDestinationTextView.setBackgroundColor(0)
            toggle=true
        }
        binding.inputDestinationTextView.setOnClickListener {
            binding.inputDestinationTextView.background = getDrawable(R.drawable.textview_focus_design)
            binding.inputStartTextView.setBackgroundColor(0)
            toggle=false
        }


        binding.button.setOnClickListener {
            // 출발지 혹은 목적지가 비어있지 않다면 실행, 스피너는 기본값이 있으므로 체크하지 않음
            if (!binding.inputStartTextView.text.isNullOrEmpty() && !binding.inputDestinationTextView.text.isNullOrEmpty()
                && !binding.inputEditStart.text.isNullOrEmpty() && !binding.inputEditDestination.text.isNullOrEmpty()) {
                var destination = binding.inputEditStart.text.toString() + "->" + binding.inputEditDestination.text.toString()
                // 현재 시간 불러오기
                val current = LocalDateTime.now()
                var hour = current.hour
                // 현재 시간에 10분 더함
                var minute = current.minute + 10
                // 60분을 초과할 경우
                if (minute >= 60) {
                    hour++
                    minute -= 60
                    // 24시를 초과할 경우
                    if (hour == 24) hour - 24
                }
                val time = hour.toString() + "시" + minute.toString() + "분"
                // 스피너에 있는 아이템 값을 읽어옴
                val maxPeople = binding.headCountSpinner.selectedItem.toString()

                /** 여기다가 넣어놓음 */

                val room = Room(
                    destination, userName, time, "0", maxPeople.replace("명", ""),
                    startLatLng.latitude.toString(), startLatLng.longitude.toString(),
                    destinationLatLng.latitude.toString(), destinationLatLng.longitude.toString())
                // 파이어베이스에 저장
                saveRoomToFirestore(room)
                Toast.makeText(
                    baseContext, "방 생성이 완료되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("attend_roomName", roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
                intent.putExtra("userName", userName)
                startActivity(intent)

            } else {
                Toast.makeText(
                    baseContext, "출발지 혹은 도착지가 입력되지 않았습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /** 이거랑 */
        binding.acbtnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("attend_roomName", roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
            intent.putExtra("userName", userName)
            startActivity(intent)
        }
    }

    /** 이거랑 */
    override fun onBackPressed() {
        super.onBackPressed()

        // Return to MainActivity and set flag for returning from ChatActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("isReturnedFromCreateActivity", true)
        intent.putExtra("attend_roomName", roomName)    // 참여 성공한 방의 RoomName을 MainActivity로 전송해줌
        intent.putExtra("userName", userName)
        startActivity(intent)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // 처음 카메라의 위치를 잡을 임의의 인천대학교 위도 경도
        val initLatLng = LatLng(37.373435, 126.632829)
        // initLatLng로 카메라 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initLatLng, 15F))
        // 출발지 마커를 시작 위치로 찍고 숨김
        var startMarker = mMap.addMarker(MarkerOptions().position(initLatLng).title("출발지"))
        startMarker?.isVisible = false
        // 도착지 마커를 시작 위치로 찍고 숨김
        var destinationMarker = mMap.addMarker(MarkerOptions().position(initLatLng).title("도착지"))
        destinationMarker?.isVisible = false
        // 맵을 클릭했을 때 이벤트
        mMap.setOnMapClickListener {
            // getAddress 호출하여 location에 주소명 저장
            var location = getAddress(this, it.latitude, it.longitude)
            // 출발지 텍스트에 포커스가 잡혀있다면
            if (toggle) {
                // 출발지 마커를 보이고 위도 경도 정보를 startMarker에 저장 및 텍스트 입력
                startMarker?.isVisible = true
                startMarker?.position = it
                startMarker?.showInfoWindow()
                startLatLng = it
                binding.inputStartTextView.setText(location)
            }
            // 도착지 텍스트에 포커스가 잡혀있다면
            else if (!toggle) {
                // 도착지 마커를 보이고 위도 경도 정보를 destinationMarker에 저장 및 텍스트 입력
                destinationMarker?.isVisible = true
                destinationMarker?.position = it
                destinationMarker?.showInfoWindow()
                destinationLatLng = it
                binding.inputDestinationTextView.setText(location)
            }
        }
    }

    open fun getAddress(mContext: Context?, lat: Double, lng: Double): String? {
        // 주소명을 저장할 변수, 위치가 안잡힐 경우 디폴트 "현재 위치를 확인 할 수 없습니다."
        var nowAddr = "현재 위치를 확인 할 수 없습니다."
        val geocoder = Geocoder(mContext!!, Locale.KOREA)
        val address: List<Address>?
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1)
                if (address != null && address.size > 0) {
                    nowAddr = address[0].getAddressLine(0).toString()
                }
                // 대한민국에서만 찍을거라 주소명에서 "대한민국"은 제외
                nowAddr = nowAddr.replace("대한민국 ", "")
            }
        } catch (e: IOException) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        return nowAddr
    }

    // 파이어베이스에 데이터 저장하는 함수
    fun saveRoomToFirestore(room: Room) {
        firestore.collection("Room")
            .document(room.destination)
            .set(room)
            .addOnSuccessListener { documentReference ->
            }
            .addOnFailureListener { exception ->
            }
    }
}