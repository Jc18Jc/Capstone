package com.example.myapplication.History

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DTO.history
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore

private val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스
private lateinit var binding: FragmentHistoryBinding
private var userName: String? = ""
private lateinit var currentUser: String

class HistoryFragment : Fragment() {
    private val firebaseData: ArrayList<history> = arrayListOf()
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val recyclerView: RecyclerView = binding.recyclerView4
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyAdapter(firebaseData)
        recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFirebaseData()
    }

    override fun onResume() {
        super.onResume()
        userName = arguments?.getString("key_userName")
        Log.d("login_success_history", userName.toString())
    }

    private fun loadFirebaseData() {
        userName = arguments?.getString("key_userName")
        Log.d("login_success_history", userName.toString())

        db.collection("User")
            .document("$userName")
            .collection("history")
            .addSnapshotListener { querySnapshot, e ->
                firebaseData.clear()
                if (e != null) {
                    Log.d("firebase_error", "failed $e!")
                    return@addSnapshotListener
                }

                for (doc in querySnapshot!!.documents) {
                    val cost = doc.getString("cost") ?: ""
                    val date = doc.getString("date") ?: ""
                    val route = doc.getString("route") ?: ""

                    val item = history(cost, date, route)
                    firebaseData.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var costText: TextView = itemView.findViewById(R.id.costText)
        var routeText: TextView = itemView.findViewById(R.id.routeText)
        var dateText: TextView = itemView.findViewById(R.id.dateText)
    }

    inner class MyAdapter(private val list: ArrayList<history>) : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.history_item, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = list[position]
            holder.costText.text = item.cost
            holder.routeText.text = item.route
            holder.dateText.text = item.date
        }
    }
}
