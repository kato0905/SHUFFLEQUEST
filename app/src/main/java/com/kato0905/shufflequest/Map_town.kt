package com.kato0905.shufflequest

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class Map_town : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_town)

        //冒険に出るボタン
        findViewById<Button>(R.id.to_field_button).setOnClickListener{
            findViewById<Button>(R.id.to_field_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.to_field_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))
            Handler().postDelayed(Runnable {
                val intent = Intent(this, Field_Forest::class.java)
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
                startActivity(intent)
            }, 500.toLong())
        }
    }
}
