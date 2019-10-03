package com.kato0905.shufflequest

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_shop.*

class Shop : Activity() {

    lateinit var realm: Realm

    class Item{
        var id: Int = 0
        var name: String = ""
        var cost: Int = 0
        var power: Int = 0
        var current: Int = 0
        var explain: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)
        realm.beginTransaction()

        //下のページを透明に
        findViewById<TableLayout>(R.id.page1).alpha = 1.toFloat()
        findViewById<TableLayout>(R.id.page2).alpha = 0.toFloat()
        findViewById<TableLayout>(R.id.page3).alpha = 0.toFloat()

        val player = realm.where(PlayerModel::class.java).findFirst()
        val item = realm.where(ItemModel::class.java).findAll()
        findViewById<TextView>(R.id.player_money).setText(""+player!!.money)

        findViewById<Button>(R.id.back_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            finish()
        }

        page_move()


        var i=1
        var st=""

        item.forEach(){
            var resID = getResources().getIdentifier("item_"+ i + "_name", "id", "com.kato0905.shufflequest")
            findViewById<TextView>(resID).setText(""+it.name)
            if(i<3) {
                st = it.name

                //購入処理
                findViewById<TextView>(resID).setOnClickListener {
                    Toast.makeText(applicationContext, "トースト" + st + "メッセージ", Toast.LENGTH_LONG).show()
                }
            }
            resID = getResources().getIdentifier("item_"+ i + "_cost", "id", "com.kato0905.shufflequest")
            findViewById<TextView>(resID).setText(" "+it.cost)
            resID = getResources().getIdentifier("item_"+ i + "_power", "id", "com.kato0905.shufflequest")
            findViewById<TextView>(resID).setText(" "+it.power)
            resID = getResources().getIdentifier("item_"+ i + "_explain", "id", "com.kato0905.shufflequest")
            findViewById<TextView>(resID).setText(" "+it.explain)
            i++
        }
    }


    fun page_move(){


        findViewById<TextView>(R.id.next_button).setOnClickListener {
            if(findViewById<TableLayout>(R.id.page1).alpha == 1.toFloat()){

                findViewById<TableLayout>(R.id.page1).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page2).alpha = 1.toFloat()
                findViewById<TableLayout>(R.id.page3).alpha = 0.toFloat()

            }else if(findViewById<TableLayout>(R.id.page2).alpha == 1.toFloat()){

                findViewById<TableLayout>(R.id.page1).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page2).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page3).alpha = 1.toFloat()

            }else if(findViewById<TableLayout>(R.id.page3).alpha == 1.toFloat()){

                findViewById<TableLayout>(R.id.page1).alpha = 1.toFloat()
                findViewById<TableLayout>(R.id.page2).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page3).alpha = 0.toFloat()

            }else{
                Log.d("system_output","Error in page_move next")
            }
        }
        findViewById<TextView>(R.id.before_button).setOnClickListener {
            if(findViewById<TableLayout>(R.id.page1).alpha == 1.toFloat()){

                findViewById<TableLayout>(R.id.page1).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page2).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page3).alpha = 1.toFloat()

            }else if(findViewById<TableLayout>(R.id.page2).alpha == 1.toFloat()){

                findViewById<TableLayout>(R.id.page1).alpha = 1.toFloat()
                findViewById<TableLayout>(R.id.page2).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page3).alpha = 0.toFloat()

            }else if(findViewById<TableLayout>(R.id.page3).alpha == 1.toFloat()){

                findViewById<TableLayout>(R.id.page1).alpha = 0.toFloat()
                findViewById<TableLayout>(R.id.page2).alpha = 1.toFloat()
                findViewById<TableLayout>(R.id.page3).alpha = 0.toFloat()

            }else{
                Log.d("system_output","Error in page_move before")
            }
        }
    }
}
