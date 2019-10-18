package com.kato0905.shufflequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.app.Activity
import android.media.AudioAttributes
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlin.math.max
import android.media.SoundPool
import android.media.MediaPlayer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class Field_Forest : Activity() {

    var current_progress = 1
    lateinit var realm: Realm
    private lateinit var soundPool: SoundPool
    private lateinit var field_bgm: MediaPlayer
    lateinit var mAdView : AdView


    override fun onResume() {
        super.onResume()

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        field_bgm = MediaPlayer.create(this, R.raw.field_bgm)
        field_bgm.setVolume(0.5f, 0.5f)
        field_bgm.setLooping(true)
        field_bgm.start()
        var se_encount = soundPool.load(this, R.raw.encount, 1)
        var se_walk = soundPool.load(this, R.raw.walk, 1)
        var se_move = soundPool.load(this, R.raw.move,1)
    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        field_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_field__forest)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        var se_encount = soundPool.load(this, R.raw.encount, 1)
        var se_walk = soundPool.load(this, R.raw.walk, 1)
        var se_move = soundPool.load(this, R.raw.move,1)

        findViewById<TextView>(R.id.back_button).setText("街へ")
        findViewById<TextView>(R.id.step).setText(current_progress.toString())


        //進むボタン
        findViewById<Button>(R.id.forward_button).setOnClickListener{
            findViewById<Button>(R.id.forward_button).alpha = 0.6.toFloat()   //ボタンを透明に
            soundPool.play(se_walk, 1.0f, 1.0f, 1, 0, 1.0f)
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.forward_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))
            current_progress++
            if(current_progress == 1){
                findViewById<TextView>(R.id.back_button).setText("街へ")
            }else if(current_progress == 2){
                findViewById<TextView>(R.id.back_button).setText("戻る")
            }
            findViewById<TextView>(R.id.step).setText(current_progress.toString())
            when(current_progress){
                in 0..15 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_grass)
                in 16..30 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_forest)
                in 31..60 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_wasteland)
                in 61..80 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_dangeon)
                in 81..99 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_devilcastle)
                100 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_vsdevil)
                else -> Log.d("system_output", "Error in forward_button")
            }

            if((0..100).random() <= 30 || current_progress == 100){
                //敵が出てくる
                soundPool.play(se_encount, 1.0f, 1.0f, 1, 0, 1.0f)
                soundPool?.release()
                field_bgm.release()
                val intent = Intent(this, Battle::class.java)
                intent.putExtra("current_progress", current_progress.toString())
                startActivity(intent)
            }
        }

        //戻るボタン
        findViewById<Button>(R.id.back_button).setOnClickListener{
            findViewById<Button>(R.id.back_button).alpha = 0.6.toFloat()
            soundPool.play(se_walk, 1.0f, 1.0f, 1, 0, 1.0f)
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.back_button).alpha = 1.toFloat()
            }, (100.toLong()))
            current_progress--

            if(current_progress == 1){
                findViewById<TextView>(R.id.back_button).setText("街へ")
            }else if(current_progress == 2){
                findViewById<TextView>(R.id.back_button).setText("戻る")
            }
            if(current_progress <= 0){
                soundPool.play(se_move, 1.0f, 1.0f, 1, 0, 1.0f)
                soundPool?.release()
                field_bgm.release()
                val intent = Intent(this, Map_town::class.java)
                finish()
                startActivity(intent)
            }

            findViewById<TextView>(R.id.step).setText(current_progress.toString())

            when(current_progress) {
                in 0..15 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_grass)
                in 16..30 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_forest)
                in 31..60 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_wasteland)
                in 61..80 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_dangeon)
                in 81..99 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_devilcastle)
                100 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_vsdevil)
                else -> Log.d("system_output", "Error in back_button")
            }

            if((0..100).random() <= 30 || current_progress == 100){
                //敵が出てくる
                soundPool.play(se_encount, 1.0f, 1.0f, 1, 0, 1.0f)
                soundPool?.release()
                field_bgm.release()
                val intent = Intent(this, Battle::class.java)
                intent.putExtra("current_progress",current_progress.toString())
                startActivity(intent)
            }
        }

        //ステータスボタン
        findViewById<Button>(R.id.status_button).setOnClickListener {
            findViewById<Button>(R.id.status_button).alpha = 0.6.toFloat()
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.status_button).alpha = 1.toFloat()
            }, (100.toLong()))
            soundPool?.release()
            field_bgm.release()
            val intent = Intent(this, Status::class.java)
            startActivity(intent)
        }
    }
}
