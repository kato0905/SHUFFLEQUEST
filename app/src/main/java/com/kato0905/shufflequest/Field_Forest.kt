package com.kato0905.shufflequest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.app.Activity
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlin.math.max

class Field_Forest : Activity() {

    var current_progress = 1
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_field__forest)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)

        findViewById<TextView>(R.id.back_button).setText("街へ")
        findViewById<TextView>(R.id.step).setText(current_progress.toString())


        //進むボタン
        findViewById<Button>(R.id.forward_button).setOnClickListener{
            findViewById<Button>(R.id.forward_button).alpha = 0.6.toFloat()   //ボタンを透明に
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
                val intent = Intent(this, Battle::class.java)
                intent.putExtra("current_progress", current_progress.toString())
                startActivity(intent)
            }
        }

        //戻るボタン
        findViewById<Button>(R.id.back_button).setOnClickListener{
            findViewById<Button>(R.id.back_button).alpha = 0.6.toFloat()
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
                val intent = Intent(this, Map_town::class.java)
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

            val intent = Intent(this, Status::class.java)
            startActivity(intent)
        }
    }

}
