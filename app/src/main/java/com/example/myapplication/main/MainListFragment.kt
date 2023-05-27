package com.example.myapplication.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DTO.taxiList
import com.example.myapplication.MainScreen.DetailRoomActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMainListBinding
import com.google.firebase.firestore.FirebaseFirestore

private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스
private lateinit var binding : FragmentMainListBinding

class MainListFragment : Fragment() {
    private lateinit var binding: FragmentMainListBinding
    private val firebaseData: ArrayList<taxiList> = arrayListOf()
    private lateinit var adapter: MyAdapter
    var fbFirestore : FirebaseFirestore? = null
    var userName:String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainListBinding.inflate(inflater, container, false)
        val recyclerView: RecyclerView = binding.recyclerView3
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyAdapter(firebaseData)
        recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        loadFirebaseData()
        adapter.notifyDataSetChanged()
    }

    private fun loadFirebaseData() {
        db.collection("Room").addSnapshotListener { querySnapshot, e ->
            //firebaseData.clear()
            if (e != null) {
                Log.d("firebase_error", "failed $e!")
                return@addSnapshotListener
            }

            for (doc in querySnapshot!!.documentChanges) {

                var destination = doc.document["destination"].toString()
                var endTime = doc.document["endTime"].toString()
                var maxPeople=""
                var nowPeople = "0"
                db.collection("Room")
                    .document(destination)
                    .collection("member")
                    .get()
                    .addOnSuccessListener {
                        nowPeople=it.size().toString()
                        maxPeople = "$nowPeople 명 / ${doc.document["maxPeople"].toString()} 명"

                        firebaseData.add(taxiList(destination, endTime, maxPeople))
                        Log.d("baby", "${maxPeople}")

                        if (firebaseData.size == querySnapshot.documentChanges.size) {
                            // 마감시간이 빠른 순서로 정렬
                            firebaseData.sortBy { it.endTime }
                            adapter.notifyDataSetChanged()
                        }
                    }
                Log.d("baby", "$firebaseData")
            }

            // 마감시간이 빠른 순서로 정렬
            firebaseData.sortBy { it.endTime }
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadRoomData(inputDestination: String) {
        firebaseData.clear()
        db.collection("Room")
            .document("$inputDestination")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("firebase_error", "failed $e!")
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.exists()) {
                    val destination = querySnapshot.getString("destination") ?: ""
                    val endTime = querySnapshot.getString("endTime") ?: ""
                    val maxPeople = querySnapshot.getString("maxPeople") ?: ""
                    val nowPeople = querySnapshot.getString("nowPeople") ?: ""

                    var calc_people = ""
                    db.collection("Room")
                        .document(destination)
                        .collection("member")
                        .get()
                        .addOnSuccessListener {
                            calc_people=it.size().toString()

                            /** Detail Room Activity로 데이터 전송하기 */
                            userName = arguments?.getString("key_userName")
                            Log.d("login_success_mainList", userName.toString())

                            val intent = Intent(activity, DetailRoomActivity::class.java)
                            intent.putExtra("key_destination", destination)
                            intent.putExtra("key_endTime", endTime)
                            intent.putExtra("key_maxPeople", maxPeople)
                            intent.putExtra("key_nowPeople", calc_people)
                            intent.putExtra("key_userName", userName)

                            startActivity(intent)
                        }
                }
            }
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var destinationText: TextView = itemView.findViewById(R.id.destination)
        var endTimeText: TextView = itemView.findViewById(R.id.endTime)
        var peopleText: TextView = itemView.findViewById(R.id.people)

        fun bind(item:taxiList){
            destinationText.text = item.destination
            endTimeText.text = item.endTime
            peopleText.text = item.maxPeople

            itemView.setOnClickListener {
                Log.d("itemClicked", item.destination)

                loadRoomData(item.destination)
            }
        }
    }

    inner class MyAdapter(private val list: ArrayList<taxiList>) : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.mainlist_item, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = list[position]
            holder.bind(item)
        }
    }
}