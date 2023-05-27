package com.example.myapplication.MainScreen

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.myapplication.DTO.Room
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCreateBinding
import com.example.myapplication.main.MainListFragment
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


class CreateFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentCreateBinding
    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var startLatLng: LatLng    // 출발지 위도 경도
    private lateinit var destinationLatLng: LatLng  // 도착지 위도 경도
    private var toggle by Delegates.notNull<Boolean>()    // toggle이 true면 출발지, false면 도착지에 주소 입력
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference

        ArrayAdapter.createFromResource(requireContext(), R.array.number, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.headCountSpinner.adapter = adapter
            }

        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toggle = true

        // 텍스트뷰를 클릭해서 포커스를 주면 해당 텍스트뷰의 백그라운드를 바꾸고 다른 텍스트뷰의 백그라운드를 없앰
        binding.inputStartTextView.setOnClickListener {
            binding.inputStartTextView.background =
                requireContext().getDrawable(R.drawable.textview_focus_design)
            binding.inputDestinationTextView.setBackgroundColor(0)
            toggle = true
        }
        binding.inputDestinationTextView.setOnClickListener {
            binding.inputDestinationTextView.background =
                requireContext().getDrawable(R.drawable.textview_focus_design)
            binding.inputStartTextView.setBackgroundColor(0)
            toggle = false
        }

        binding.button.setOnClickListener {
            // 출발지 혹은 목적지가 비어있지 않다면 실행, 스피너는 기본값이 있으므로 체크하지 않음
            if (!binding.inputStartTextView.text.isNullOrEmpty() && !binding.inputDestinationTextView.text.isNullOrEmpty()) {
                var destination = ""
                destination =
                    binding.inputStartTextView.text.toString() + "->" + binding.inputDestinationTextView.text.toString()
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
                val room = Room(
                    destination, "호스트", time, "1", maxPeople.replace("명", ""),
                    startLatLng.latitude.toString(), startLatLng.longitude.toString(),
                    destinationLatLng.latitude.toString(), destinationLatLng.longitude.toString()
                )
                // 파이어베이스에 저장
                saveRoomToFirestore(room)
                Toast.makeText(
                    requireContext(), "저장되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

/*                // MainlistFragment로 전환
                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                val mainlistFragment = MainListFragment()

                fragmentTransaction.replace(R.id.frame_layout, mainlistFragment)

                // 트랜잭션 커밋
                fragmentTransaction.commit()*/

            } else {
                Toast.makeText(
                    requireContext(), "출발지 혹은 도착지가 입력되지 않았습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
            var location = getAddress(requireContext(), it.latitude, it.longitude)
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

    private fun getAddress(mContext: Context?, lat: Double, lng: Double): String? {
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
    private fun saveRoomToFirestore(room: Room) {
        firestore.collection("Room")
            .document(room.destination)
            .set(room)
            .addOnSuccessListener { documentReference ->
            }
            .addOnFailureListener { exception ->
            }
    }

}