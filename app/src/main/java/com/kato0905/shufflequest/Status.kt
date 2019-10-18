package com.kato0905.shufflequest

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration

class Status : Activity() {
    lateinit var realm: Realm
    private lateinit var soundPool: SoundPool
    private lateinit var status_bgm: MediaPlayer

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

        status_bgm = MediaPlayer.create(this, R.raw.status_bgm)
        status_bgm.setVolume(0.5f, 0.5f)
        status_bgm.setLooping(true)
        status_bgm.start()
    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        status_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)
        realm.beginTransaction()

        val player = realm.where(PlayerModel::class.java).findFirst()
        var i = 0
        var magic_element = 0
        var item_element = 0

        //所持している魔法
        var load_notset_magic = realm.where(MagicModel::class.java).equalTo("canuse", 1.toInt()).findAll()
        //セットしている魔法（３つ）
        val load_set_magic= realm.where(MagicModel::class.java).equalTo("canuse", 2.toInt()).findAll()
        //所持しているアイテム
        var load_item = realm.where(ItemModel::class.java).greaterThan("current",0).notEqualTo("set", 1.toInt()).findAll()
        //現在セットしているアイテム
        val current_item = realm.where(ItemModel::class.java).equalTo("set", 1.toInt()).findFirst()

        load_notset_magic.forEach(){
            magic_element++
        }

        load_item.forEach(){
            item_element++
        }

        var notset_magic_name: Array<String?> = arrayOfNulls(magic_element)
        var set_magic_id: Array<Int> = arrayOf(1,2,3)
        var set_magic_name: Array<String> = arrayOf("1","2","3")
        var item_1: Array<String?> = arrayOfNulls(item_element)
        var item_text: Array<String?> = arrayOfNulls(item_element)
        var notset_magic_text: Array<String?> = arrayOfNulls(magic_element)
        var set_magic_text: Array<String?> = arrayOfNulls(3)


        load_notset_magic.forEach(){
            notset_magic_name[i] = it.name
            notset_magic_text[i] = it.name+" 消費MP"+it.mp+" 効果"+it.power
            i++
        }

        i = 0

        load_set_magic.forEach(){
            set_magic_id[i] = it.id
            set_magic_name[i] = it.name
            set_magic_text[i] = it.name+"MP"+it.mp+"効果"+it.power
            i++
    }

        i = 0

        load_item.forEach(){
            item_1[i] = it.name
            item_text[i] = it.name+" ("+it.current+"個)"
            i++
        }

        //アイテムセット
        findViewById<Button>(R.id.item_button).setOnClickListener {
            AlertDialog.Builder(this)
                    .setItems(item_text, { dialog, which ->
                        //今からセットするアイテム
                        val select_item = realm.where(ItemModel::class.java).equalTo("name", item_1[which]).findFirst()
                        select_item!!.set = 1
                        current_item!!.set = 0
                        realm.commitTransaction()
                        realm.close()
                        val intent = Intent(this, Status::class.java)
                        soundPool?.release()
                        status_bgm.release()
                        finish()
                        startActivity(intent)
                    }).setPositiveButton("BACK", { dialog, which ->

                    })
                    .show()
        }

        //魔法セット
        findViewById<Button>(R.id.magic_button1).setOnClickListener {
            AlertDialog.Builder(this)
                    .setItems(notset_magic_text, { dialog, which ->
                        val select_magic = realm.where(MagicModel::class.java).equalTo("name", notset_magic_name[which]).findFirst()
                        val before_magic = realm.where(MagicModel::class.java).equalTo("id", set_magic_id[0]).findFirst()
                        select_magic!!.canuse = 2
                        before_magic!!.canuse = 1
                        realm.commitTransaction()
                        realm.close()
                        val intent = Intent(this, Status::class.java)
                        soundPool?.release()
                        status_bgm.release()
                        finish()
                        startActivity(intent)
                    }).setPositiveButton("BACK", { dialog, which ->

                    })
                    .show()
        }
        findViewById<Button>(R.id.magic_button2).setOnClickListener {
            AlertDialog.Builder(this)
                    .setItems(notset_magic_text, { dialog, which ->
                        val select_magic = realm.where(MagicModel::class.java).equalTo("name", notset_magic_name[which]).findFirst()
                        val before_magic = realm.where(MagicModel::class.java).equalTo("id", set_magic_id[1]).findFirst()
                        select_magic!!.canuse = 2
                        before_magic!!.canuse = 1
                        realm.commitTransaction()
                        realm.close()
                        val intent = Intent(this, Status::class.java)
                        soundPool?.release()
                        status_bgm.release()
                        finish()
                        startActivity(intent)
                    }).setPositiveButton("BACK", { dialog, which ->

                    })
                    .show()
        }
        findViewById<Button>(R.id.magic_button3).setOnClickListener {
            AlertDialog.Builder(this)
                    .setItems(notset_magic_text, { dialog, which ->
                        val select_magic = realm.where(MagicModel::class.java).equalTo("name", notset_magic_name[which]).findFirst()
                        val before_magic = realm.where(MagicModel::class.java).equalTo("id", set_magic_id[2]).findFirst()
                        select_magic!!.canuse = 2
                        before_magic!!.canuse = 1
                        realm.commitTransaction()
                        realm.close()
                        val intent = Intent(this, Status::class.java)
                        soundPool?.release()
                        status_bgm.release()
                        finish()
                        startActivity(intent)
                    }).setPositiveButton("BACK", { dialog, which ->

                    })
                    .show()
        }

        //シャッフル
        findViewById<Button>(R.id.shuffle_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            val intent = Intent(this, Shuffle::class.java)
            soundPool?.release()
            status_bgm.release()
            finish()
            startActivity(intent)
        }

        //戻るボタン
        findViewById<Button>(R.id.back_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            soundPool?.release()
            status_bgm.release()
            finish()
        }

        //プレイヤーステータス表示
        findViewById<TextView>(R.id.player_name).setText(player!!.name.toString())
        findViewById<TextView>(R.id.player_current_hp).setText(player.current_hp.toString())
        findViewById<TextView>(R.id.player_current_mp).setText(player.current_mp.toString())
        findViewById<TextView>(R.id.player_maxhp).setText(" / "+player.hp.toString())
        findViewById<TextView>(R.id.player_maxmp).setText(" / "+player.mp.toString())
        findViewById<TextView>(R.id.player_attack).setText(player.attack.toString())
        findViewById<TextView>(R.id.player_defense).setText(player.defense.toString())
        findViewById<TextView>(R.id.player_mdef).setText(player.mdef.toString())
        findViewById<TextView>(R.id.player_speed).setText(player.speed.toString())
        findViewById<TextView>(R.id.player_dex).setText(player.dex.toString())
        findViewById<TextView>(R.id.player_money).setText(player.money.toString())

        //魔法名前表示
        findViewById<Button>(R.id.magic_button1).setText(set_magic_text[0])
        findViewById<Button>(R.id.magic_button2).setText(set_magic_text[1])
        findViewById<Button>(R.id.magic_button3).setText(set_magic_text[2])

        //アイテム名表示
        findViewById<Button>(R.id.item_button).setText(current_item!!.name+"("+current_item.current+"個) 効果"+current_item.power)
    }
}
