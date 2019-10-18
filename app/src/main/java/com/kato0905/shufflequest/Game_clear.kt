package com.kato0905.shufflequest

import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView

class Game_clear : Activity() {
    private lateinit var soundPool: SoundPool
    private lateinit var clear_bgm: MediaPlayer

    override fun onResume(){
        super.onResume()

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        clear_bgm = MediaPlayer.create(this, R.raw.clear_bgm)
        clear_bgm.setVolume(0.5f, 0.5f)
        clear_bgm.setLooping(true)
        clear_bgm.start()

    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        clear_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_clear)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

    }
    override fun onTouchEvent(event: MotionEvent) :Boolean {

        when(event.getAction()) {
            MotionEvent.ACTION_UP -> {
                val intent = Intent(this, Title::class.java)
                soundPool?.release()
                clear_bgm.release()
                finish()
                startActivity(intent)
            }
        }
        return super.onTouchEvent(event)
    }
}