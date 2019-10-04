package com.kato0905.shufflequest

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import io.realm.Realm
import io.realm.RealmConfiguration
import org.w3c.dom.Text
import java.util.*

class Shuffle : Activity() {

    lateinit var realm: Realm

    class Magic{
        var id: Int = 0
        var name: String =""
        var mp: Int = 0
        var power: Int = 0
        var explain: String = ""
    }

    var return_num = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuffle)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)
        realm.beginTransaction()


        val magic = arrayOf(Magic(), Magic(), Magic())
        val myApp = this.application as MyApp


        val origin_player = realm.where(OriginPlayerModel::class.java).findFirst()
        val origin_monster = realm.where(OriginMonsterModel::class.java).findAll()
        val origin_magic = realm.where(OriginMagicModel::class.java).findAll()
        val origin_item = realm.where(OriginItemModel::class.java).findAll()
        val player = realm.where(PlayerModel::class.java).findFirst()
        val item = realm.where(ItemModel::class.java).equalTo("set",1.toInt()).findFirst()
        val load_magic = realm.where(MagicModel::class.java).equalTo("canuse", 2.toInt()).findAll()

        var i = 0
        load_magic.forEach(){
            magic[i].id = it.id
            magic[i].name = it.name
            magic[i].mp = it.mp
            magic[i].power = it.power
            magic[i].explain = it.explain
            i++
        }

        /*
        シャッフル処理
        モンスター6種(HP, MP, ATTACK, DEFENSE, SPEED, DEX)
        プレイヤー7種(HP, MP, ATTACK, DEFENSE, SPEED, DEX, MDEF)
        魔法2種(MP, POWER)
        アイテム1種(POWER)
         */

        do_shuffle(3)



        // 戻るボタン
        findViewById<Button>(R.id.back_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            val intent = Intent(this, Status::class.java)
            finish()
            startActivity(intent)
        }

        //プレイヤーステータス表示
        findViewById<TextView>(R.id.player_name).setText(player!!.name)
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

        //魔法表示
        findViewById<TextView>(R.id.magic1_name).setText(magic[0].name.toString()+" ")
        findViewById<TextView>(R.id.magic2_name).setText(magic[1].name.toString()+" ")
        findViewById<TextView>(R.id.magic3_name).setText(magic[2].name.toString()+" ")
        findViewById<TextView>(R.id.magic1_mp).setText(magic[0].mp.toString()+" ")
        findViewById<TextView>(R.id.magic2_mp).setText(magic[1].mp.toString()+" ")
        findViewById<TextView>(R.id.magic3_mp).setText(magic[2].mp.toString()+" ")
        findViewById<TextView>(R.id.magic1_power).setText(magic[0].power.toString()+" ")
        findViewById<TextView>(R.id.magic2_power).setText(magic[1].power.toString()+" ")
        findViewById<TextView>(R.id.magic3_power).setText(magic[2].power.toString()+" ")
        findViewById<TextView>(R.id.magic1_explain).setText(magic[0].explain.toString()+" ")
        findViewById<TextView>(R.id.magic2_explain).setText(magic[1].explain.toString()+" ")
        findViewById<TextView>(R.id.magic3_explain).setText(magic[2].explain.toString()+" ")

        //アイテム表示
        findViewById<TextView>(R.id.item_name).setText(item!!.name.toString()+" ")
        findViewById<TextView>(R.id.item_current).setText(item!!.current.toString()+" ")
        findViewById<TextView>(R.id.item_power).setText(item!!.power.toString()+" ")
        findViewById<TextView>(R.id.item_explain).setText(item!!.explain.toString())
    }



    fun do_shuffle(num: Int):Int{
        val myApp = this.application as MyApp

        val origin_player = realm.where(OriginPlayerModel::class.java).findFirst()
        val item = realm.where(ItemModel::class.java).equalTo("set",1.toInt()).findFirst()
        val origin_item = realm.where(OriginItemModel::class.java).equalTo("id",item!!.id.toInt()).findFirst()
        val load_magic = realm.where(MagicModel::class.java).equalTo("canuse", 2.toInt()).findAll()

        var i = 0
        val origin_magic = arrayOf(Magic(), Magic(), Magic())

        load_magic.forEach(){
            val tem_magic = realm.where(OriginMagicModel::class.java).equalTo("id", it.id).findFirst()
            origin_magic[i].mp = tem_magic!!.mp
            origin_magic[i].power = tem_magic.power
            i++
        }


        if(num in 1..myApp.monster_num*6){

            //モンスター
            val id = num/6 + 1
            val tem_monster = realm.where(OriginMonsterModel::class.java).equalTo("id",id).findFirst()
            when(num%6){
                1 -> return tem_monster!!.hp
                2 -> return tem_monster!!.mp
                3 -> return tem_monster!!.attack
                4 -> return tem_monster!!.defense
                5 -> return tem_monster!!.speed
                0 -> return tem_monster!!.dex
            }

        }else if(num in myApp.monster_num*6+1..myApp.monster_num*6+myApp.player_num*7){

            //プレイヤー
            when((num - myApp.monster_num*6)%7){
                1 -> return origin_player!!.hp
                2 -> return origin_player!!.mp
                3 -> return origin_player!!.attack
                4 -> return origin_player!!.defense
                5 -> return origin_player!!.speed
                6 -> return origin_player!!.dex
                0 -> return origin_player!!.mdef
            }

        }else if(num in myApp.monster_num*6+myApp.player_num*7+1..myApp.monster_num*6+myApp.player_num*7+myApp.magic_num*2){

            //マジック
            val id = (num - myApp.monster_num*6+myApp.player_num*7)/myApp.magic_num
            when((num - myApp.monster_num*6+myApp.player_num*7)%2){
                1 -> return origin_magic[id-1].mp
                0 -> return origin_magic[id-1].power
            }

        }else if(num in myApp.monster_num*6+myApp.player_num*7+myApp.magic_num*2+1..myApp.monster_num*6+myApp.player_num*7+myApp.magic_num*2+myApp.item_num*1){

            //アイテム
            return origin_item!!.power
        }

        return -1

    }
}
