package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityRoomStateBinding

class RoomStateActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRoomStateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomStateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.menuBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_history -> startActivity(Intent(this,HistoryActivity::class.java))
                R.id.menu_user -> startActivity(Intent(this, UserActivity::class.java))
            }
            true
        }
        val listItems=ArrayList<Bookmark>()
        val a = Bookmark(1, "인천대입구역", "인천대학교 송도캠퍼스", 5000, 15)
        val b = Bookmark(2, "인천대입구역", "인천대학교 송도캠퍼스", 6000, 15)
        val c = Bookmark(3, "인천대입구역", "인천대학교 송도캠퍼스", 7000, 15)
        val d = Bookmark(4, "인천대입구역", "인천대학교 송도캠퍼스", 8000, 15)
        val e = Bookmark(5, "인천대입구역", "인천대학교 송도캠퍼스", 9000, 15)

        listItems.add(a)
        listItems.add(b)
        listItems.add(c)
        listItems.add(d)
        listItems.add(e)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = MyAdapter(listItems)
        recyclerView.adapter = adapter
    }
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var startTextView: TextView = itemView.findViewById(R.id.startTextView)
        var destinationTextView: TextView = itemView.findViewById(R.id.destinationTextView)
    }

    class MyAdapter(var list:ArrayList<Bookmark>): RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.incomplete_item, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.startTextView.text=list[position].start
            holder.destinationTextView.text=list[position].destination

        }
    }


}