package com.kato0905.shufflequest2

import android.app.Activity
import android.app.AlertDialog
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import io.realm.Realm
import io.realm.RealmConfiguration

class Shop : Activity() {

    lateinit var realm: Realm
    private lateinit var soundPool: SoundPool
    private lateinit var shop_bgm: MediaPlayer

    class Item{
        var id: Int = 0
        var name: String = ""
        var cost: Int = 0
        var power: Int = 0
        var current: Int = 0
        var explain: String = ""
    }

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

        shop_bgm = MediaPlayer.create(this, R.raw.shop_bgm)
        shop_bgm.setVolume(0.5f, 0.5f)
        shop_bgm.setLooping(true)
        shop_bgm.start()

    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        shop_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        var se_buy = soundPool.load(this, R.raw.buy, 1)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)
        realm.beginTransaction()

        //下のページを透明に
        findViewById<TableLayout>(R.id.page1).setVisibility(View.VISIBLE)
        findViewById<TableLayout>(R.id.page2).setVisibility(View.GONE)
        findViewById<TableLayout>(R.id.page3).setVisibility(View.GONE)

        val player = realm.where(PlayerModel::class.java).findFirst()
        val item = realm.where(ItemModel::class.java).findAll()
        findViewById<TextView>(R.id.player_money).setText(""+player!!.money)

        findViewById<Button>(R.id.back_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            soundPool?.release()
            shop_bgm.release()
            finish()
        }

        page_move()

        var i = 1
        var item_name = ""


        item.forEach(){
            var ItemID = getResources().getIdentifier("item_"+ i + "_name", "id", "com.kato0905.shufflequest2")
            findViewById<TextView>(ItemID).setText(""+it.name)

            //購入処理
            findViewById<TextView>(ItemID).setOnClickListener {
                item_name = findViewById<TextView>(ItemID).text.toString()

                val buy_item = realm.where(ItemModel::class.java).equalTo("name", item_name).findFirst()

                if(buy_item!!.cost <= player.money && buy_item.current < buy_item.max){
                    //購入可能
                    AlertDialog.Builder(this)
                            .setMessage("購入しますか？(現在の所持数:"+buy_item.current+")")
                            .setPositiveButton("YES", { dialog, which ->
                                player.money -= buy_item.cost
                                buy_item.current++
                                findViewById<TextView>(R.id.player_money).setText(""+player!!.money)
                                soundPool.play(se_buy, 1.0f, 1.0f, 1, 0, 1.0f)
                                realm.commitTransaction()
                                realm.beginTransaction()
                            })
                            .setNegativeButton("NO",{dialog, which ->

                            })
                            .show()
                }else if(buy_item.cost > player.money){
                    // 購入不可能(お金が足りない)
                    AlertDialog.Builder(this)
                            .setMessage("お金が足りない")
                            .setPositiveButton("BACK", { dialog, which ->

                            })
                            .show()
                }else if(buy_item.current >= buy_item.max){
                    // 購入不可能(最大数所持している)
                    AlertDialog.Builder(this)
                            .setMessage("最大数所持している("+buy_item.current+")個")
                            .setPositiveButton("BACK", { dialog, which ->

                            })
                            .show()
                }else{
                    Log.d("system_output", "Error in buy item")
                }
            }

            var resID = getResources().getIdentifier("item_"+ i + "_cost", "id", "com.kato0905.shufflequest2")
            findViewById<TextView>(resID).setText(" "+it.cost)

            resID = getResources().getIdentifier("item_"+ i + "_power", "id", "com.kato0905.shufflequest2")
            findViewById<TextView>(resID).setText(" "+it.power)

            resID = getResources().getIdentifier("item_"+ i + "_explain", "id", "com.kato0905.shufflequest2")
            findViewById<TextView>(resID).setText(" "+it.explain)
            i++
        }
    }


    fun page_move(){

        findViewById<TextView>(R.id.next_button).setOnClickListener {
            if(findViewById<TableLayout>(R.id.page1).visibility == View.VISIBLE){

                findViewById<TableLayout>(R.id.page1).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page2).setVisibility(View.VISIBLE)
                findViewById<TableLayout>(R.id.page3).setVisibility(View.GONE)

            }else if(findViewById<TableLayout>(R.id.page2).visibility == View.VISIBLE){

                findViewById<TableLayout>(R.id.page1).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page2).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page3).setVisibility(View.VISIBLE)

            }else if(findViewById<TableLayout>(R.id.page3).visibility == View.VISIBLE){

                findViewById<TableLayout>(R.id.page1).setVisibility(View.VISIBLE)
                findViewById<TableLayout>(R.id.page2).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page3).setVisibility(View.GONE)

            }else{
                Log.d("system_output","Error in page_move next")
            }
        }

        findViewById<TextView>(R.id.before_button).setOnClickListener {
            if(findViewById<TableLayout>(R.id.page1).visibility == View.VISIBLE){

                findViewById<TableLayout>(R.id.page1).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page2).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page3).setVisibility(View.VISIBLE)

            }else if(findViewById<TableLayout>(R.id.page2).visibility == View.VISIBLE){

                findViewById<TableLayout>(R.id.page1).setVisibility(View.VISIBLE)
                findViewById<TableLayout>(R.id.page2).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page3).setVisibility(View.GONE)

            }else if(findViewById<TableLayout>(R.id.page3).visibility == View.VISIBLE){

                findViewById<TableLayout>(R.id.page1).setVisibility(View.GONE)
                findViewById<TableLayout>(R.id.page2).setVisibility(View.VISIBLE)
                findViewById<TableLayout>(R.id.page3).setVisibility(View.GONE)

            }else{
                Log.d("system_output","Error in page_move before")
            }
        }
    }
}
