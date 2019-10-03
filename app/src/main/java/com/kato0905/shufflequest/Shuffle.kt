package com.kato0905.shufflequest

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import io.realm.Realm
import io.realm.RealmConfiguration

class Shuffle : Activity() {

    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuffle)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)
        realm.beginTransaction()


        //戻るボタン
        findViewById<Button>(R.id.back_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            val intent = Intent(this, Status::class.java)
            finish()
            startActivity(intent)
        }
    }
}
