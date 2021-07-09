package com.DeepaliVerma.heyya

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


const val UID="uid"
const val NAME="name"
const val IMAGE="photo"
class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }
}

class ScreenSlideAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa){
    override fun getItemCount()=2

    override fun createFragment(position: Int): Fragment =when(position){

        0-> InboxFragment()
        else-> PeopleFragment1()
    }

}