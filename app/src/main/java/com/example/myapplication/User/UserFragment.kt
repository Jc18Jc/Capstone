package com.example.myapplication.User

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityUserBinding

class UserFragment : Fragment() {
    private lateinit var binding : ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ActivityUserBinding.inflate(inflater, container, false)
        binding.menuBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                //R.id.menu_home -> startActivity(Intent(this, MainActivity::class.java))
                //R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java))
            }
            true
        }

        return inflater.inflate(R.layout.fragment_user, container, false)
    }
}