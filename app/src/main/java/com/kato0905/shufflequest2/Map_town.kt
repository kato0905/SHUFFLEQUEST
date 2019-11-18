package com.kato0905.shufflequest2

import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button

class Map_town : Activity() {

    private lateinit var soundPool: SoundPool
    private lateinit var town_bgm: MediaPlayer

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

        town_bgm = MediaPlayer.create(this, R.raw.town_bgm)
        town_bgm.setVolume(0.5f, 0.5f)
        town_bgm.setLooping(true)
        town_bgm.start()

    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        town_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_town)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        //冒険に出るボタン
        findViewById<Button>(R.id.to_field_button).setOnClickListener{
            findViewById<Button>(R.id.to_field_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.to_field_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))
            Handler().postDelayed(Runnable {
                val intent = Intent(this, Field_Forest::class.java)
                soundPool?.release()
                town_bgm.release()
                finish()
                startActivity(intent)
            }, (500.toLong()))
        }

        //店に入るボタン
        findViewById<Button>(R.id.shop_button).setOnClickListener{
            findViewById<Button>(R.id.shop_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.shop_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, 100.toLong())
            Handler().postDelayed(Runnable {
                val intent = Intent(this, Shop::class.java)
                soundPool?.release()
                town_bgm.release()
                startActivity(intent)
            }, 500.toLong())
        }

        //ステータスボタン
        findViewById<Button>(R.id.status_button).setOnClickListener{
            findViewById<Button>(R.id.status_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.status_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, 100.toLong())
            Handler().postDelayed(Runnable {
                val intent = Intent(this, Status::class.java)
                soundPool?.release()
                town_bgm.release()
                startActivity(intent)
            }, 500.toLong())
        }
    }
}
