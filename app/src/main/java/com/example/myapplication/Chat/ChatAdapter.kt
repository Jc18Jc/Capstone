package com.example.myapplication.Chat

import android.graphics.Color
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

const val my_chat = 1
const val your_chat = 2

class ChatAdapter(val currentUser: String, val itemList: ArrayList<ChatLayout>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        /*val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
        return ViewHolder(view)*/
        val view:View?
        return when(viewType) {
            my_chat -> {
                view =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
                MultiViewHolder1(view)
            }
            else -> {
                view =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_layout_v2, parent, false)
                MultiViewHolder2(view)
            }
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return itemList[position].type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(itemList[position].type) {
            my_chat -> {
                (holder as MultiViewHolder1).bind(itemList[position])
                holder.setIsRecyclable(false)
            }
            else -> {
                (holder as MultiViewHolder2).bind(itemList[position])
                holder.setIsRecyclable(false)
            }
        }
    }

    inner class MultiViewHolder1(itemView: View): RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.chat_card_view)
        val nickname: TextView = itemView.findViewById(R.id.chat_tv_nickname)
        val contents: TextView = itemView.findViewById(R.id.chat_tv_contents)
        val time: TextView = itemView.findViewById(R.id.chat_tv_time)

        fun bind(item: ChatLayout) {
            nickname.text = item.nickname
            contents.text = item.contents
            time.text = item.time
        }
    }

    inner class MultiViewHolder2(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = itemView.findViewById(R.id.chat_card_view2)
        val nickname: TextView = itemView.findViewById(R.id.chat_tv_nickname2)
        val contents: TextView = itemView.findViewById(R.id.chat_tv_contents2)
        val time: TextView = itemView.findViewById(R.id.chat_tv_time2)

        fun bind(item: ChatLayout) {
            nickname.text = item.nickname
            contents.text = item.contents
            time.text = item.time
        }
    }
}