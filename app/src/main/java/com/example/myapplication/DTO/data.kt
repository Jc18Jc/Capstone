package com.example.myapplication.DTO

import com.google.firebase.Timestamp

class Bookmark(val number:Int, val start:String, val destination:String, val cost: Int, val time:Int)
class taxiList(val destination:String, val endTime:String, val maxPeople:String)
class user(val email:String, val name:String, val uid:String)
class history(val cost:String, val date:String, val route:String)

data class Room(val destination: String,
                val host:String,
                val endTime: String,
                val nowPeople: String,
                val maxPeople: String,
                val startLat: String,
                val startLng: String,
                val destinationLat: String,
                val destinationLng: String)

data class Chat(val contents:String, val nickname: String, val time: Timestamp)