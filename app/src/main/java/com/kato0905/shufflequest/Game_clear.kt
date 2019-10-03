package com.kato0905.shufflequest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView

class Game_clear : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_clear)
    }
    override fun onTouchEvent(event: MotionEvent) :Boolean {

        when(event.getAction()) {
            MotionEvent.ACTION_UP -> {
                val intent = Intent(this, Title::class.java)
                finish()
                startActivity(intent)
            }
        }
        return super.onTouchEvent(event)
    }
}