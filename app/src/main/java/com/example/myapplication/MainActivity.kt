package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuBottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_history -> startActivity(Intent(this,HistoryActivity::class.java))
                R.id.menu_user -> startActivity(Intent(this, UserActivity::class.java))
            }
            true
        }
        binding.createLayout.setOnClickListener (View.OnClickListener { startActivity(Intent(this,CreateActivity::class.java)) })
        binding.joinLayout.setOnClickListener (View.OnClickListener { startActivity(Intent(this,JoinActivity::class.java)) })
        binding.selectBookmarkLayout.setOnClickListener (View.OnClickListener { startActivity(Intent(this,SelectBookmarkActivity::class.java)) })

    }
}