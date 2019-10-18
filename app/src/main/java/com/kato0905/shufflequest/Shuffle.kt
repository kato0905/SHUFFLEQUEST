package com.kato0905.shufflequest

import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import org.w3c.dom.Text
import java.util.*

class Shuffle : Activity() {

    lateinit var realm: Realm
    private lateinit var soundPool: SoundPool
    private lateinit var shuffle_bgm: MediaPlayer

    class Magic{
        var id: Int = 0
        var name: String =""
        var mp: Int = 0
        var power: Int = 0
        var explain: String = ""
    }

    var return_num = -1

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

        var se_shuffle = soundPool.load(this, R.raw.shuffle, 1)

        shuffle_bgm = MediaPlayer.create(this, R.raw.status_bgm)
        shuffle_bgm.setVolume(0.5f, 0.5f)
        shuffle_bgm.setLooping(true)
        shuffle_bgm.start()

    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        shuffle_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuffle)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        var se_shuffle = soundPool.load(this, R.raw.shuffle, 1)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)
        realm.beginTransaction()


        val magic = arrayOf(Magic(), Magic(), Magic())
        val myApp = this.application as MyApp

        val shuffle_num =myApp.monster_num*6 + myApp.player_num*7 + myApp.magic_num*2 + myApp.item_num*1

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

        val list = ArrayList<Int>()

        for (i in 1..shuffle_num) {
            list.add(i)
        }

        Collections.shuffle(list)

        //シャッフルボタン
        findViewById<Button>(R.id.shuffle_button).setOnClickListener{
            soundPool.play(se_shuffle, 1.0f, 1.0f, 1, 0, 1.0f)
            /*

        シャッフル処理
        モンスター6種(HP, MP, ATTACK, DEFENSE, SPEED, DEX)
        プレイヤー7種(HP, MP, ATTACK, DEFENSE, SPEED, DEX, MDEF)
        魔法2種(MP, POWER)
        アイテム1種(POWER)

         */


            for (num in 1..shuffle_num) {

                if (num in 1..myApp.monster_num * 6) {

                    //モンスター
                    val id = (num-1) / 6 + 1
                    val tem_monster = realm.where(EnemyModel::class.java).equalTo("id", id).findFirst()
                    val tem_push_monster = realm.where(PushMonsterModel::class.java).equalTo("id", id).findFirst()
                    when (num % 6) {
                        1 -> {
                            tem_monster!!.hp = do_shuffle(list[num]).first
                            tem_push_monster!!.hp = do_shuffle(list[num]).second
                        }
                        2 -> {
                            tem_monster!!.mp = do_shuffle(list[num]).first
                            tem_push_monster!!.mp = do_shuffle(list[num]).second
                        }
                        3 -> {
                            tem_monster!!.attack = do_shuffle(list[num]).first
                            tem_push_monster!!.attack = do_shuffle(list[num]).second
                        }
                        4 -> {
                            tem_monster!!.defense = do_shuffle(list[num]).first
                            tem_push_monster!!.defense = do_shuffle(list[num]).second
                        }
                        5 -> {
                            tem_monster!!.speed = do_shuffle(list[num]).first
                            tem_push_monster!!.speed = do_shuffle(list[num]).second
                        }
                        0 -> {
                            tem_monster!!.dex = do_shuffle(list[num]).first
                            tem_push_monster!!.dex = do_shuffle(list[num]).second
                        }
                    }

                } else if (num in myApp.monster_num * 6 + 1..myApp.monster_num * 6 + myApp.player_num * 7) {

                    //プレイヤー
                    val tem_push_player = realm.where(PushPlayerModel::class.java).findFirst()
                    when ((num - myApp.monster_num * 6) % 7) {
                        1 -> {
                            val before_hp = player!!.hp
                            player.hp = do_shuffle(list[num]).first
                            player.current_hp = player.hp * player.current_hp/before_hp
                            tem_push_player!!.hp = do_shuffle(list[num]).second
                        }
                        2 -> {
                            val before_mp = player!!.mp
                            player.mp = do_shuffle(list[num]).first
                            player.current_mp = player.mp * player.current_mp/before_mp
                            tem_push_player!!.mp = do_shuffle(list[num]).second
                        }
                        3 -> {
                            player!!.attack = do_shuffle(list[num]).first
                            tem_push_player!!.attack = do_shuffle(list[num]).second
                        }
                        4 -> {
                            player!!.defense = do_shuffle(list[num]).first
                            tem_push_player!!.defense = do_shuffle(list[num]).second
                        }
                        5 -> {
                            player!!.speed = do_shuffle(list[num]).first
                            tem_push_player!!.speed = do_shuffle(list[num]).second
                        }
                        6 -> {
                            player!!.dex = do_shuffle(list[num]).first
                            tem_push_player!!.dex = do_shuffle(list[num]).second
                        }
                        0 -> {
                            player!!.mdef = do_shuffle(list[num]).first
                            tem_push_player!!.mdef = do_shuffle(list[num]).second
                        }
                    }

                } else if (num in myApp.monster_num * 6 + myApp.player_num * 7 + 1..myApp.monster_num * 6 + myApp.player_num * 7 + myApp.magic_num * 2) {

                    //マジック
                    val id = (num - myApp.monster_num * 6 - myApp.player_num * 7 - 1) / 2// 0~5/2
                    i = 0
                    load_magic.forEach() {
                        if (id == i) {
                            when ((num - myApp.monster_num * 6 - myApp.player_num * 7) % 2) {
                                1 -> it.mp = do_shuffle(list[num]).first
                                0 -> it.power = do_shuffle(list[num]).first
                            }
                        }
                        i++
                    }
                } else if (num in myApp.monster_num * 6 + myApp.player_num * 7 + myApp.magic_num * 2 + 1..myApp.monster_num * 6 + myApp.player_num * 7 + myApp.magic_num * 2 + myApp.item_num * 1) {
                    //アイテム
                    item!!.power = do_shuffle(list[0]).first
                }
            }
            realm.commitTransaction()
            display()
            Collections.shuffle(list)
            realm.beginTransaction()
        }


        // 戻るボタン
        findViewById<Button>(R.id.back_button).setOnClickListener{
            realm.commitTransaction()
            realm.close()
            val intent = Intent(this, Status::class.java)
            soundPool?.release()
            shuffle_bgm.release()
            finish()
            startActivity(intent)
        }

        display()

    }


    fun display(){

        val magic = arrayOf(Magic(), Magic(), Magic())

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

        //プレイヤープッシュ処理
        val push_player = realm.where(PushPlayerModel::class.java).findFirst()
        findViewById<TextView>(R.id.player_maxhp).setOnClickListener {
            Toast.makeText(applicationContext, push_player!!.hp, Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.player_maxmp).setOnClickListener {
            Toast.makeText(applicationContext, push_player!!.mp, Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.player_attack).setOnClickListener {
            Toast.makeText(applicationContext, push_player!!.attack, Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.player_defense).setOnClickListener {
            Toast.makeText(applicationContext, push_player!!.defense, Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.player_mdef).setOnClickListener {
            Toast.makeText(applicationContext, push_player!!.mdef, Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.player_speed).setOnClickListener {
            Toast.makeText(applicationContext, push_player!!.speed, Toast.LENGTH_SHORT).show()
        }

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



    /*

    受け取った数値がOriginalなデータベースにおいて、どのモデルのどの項目に当てはまるか、
    その項目の値(Int)とそれの解説(string)を返す

     */

    fun do_shuffle(num: Int):Pair<Int,String>{
        val myApp = this.application as MyApp

        val origin_player = realm.where(OriginPlayerModel::class.java).findFirst()
        val item = realm.where(ItemModel::class.java).equalTo("set",1.toInt()).findFirst()
        val origin_item = realm.where(OriginItemModel::class.java).equalTo("id",item!!.id.toInt()).findFirst()
        val load_magic = realm.where(MagicModel::class.java).equalTo("canuse", 2.toInt()).findAll()

        var i = 0
        val origin_magic = arrayOf(Magic(), Magic(), Magic())

        load_magic.forEach(){
            val tem_magic = realm.where(OriginMagicModel::class.java).equalTo("id", it.id).findFirst()
            origin_magic[i].name = tem_magic!!.name
            origin_magic[i].mp = tem_magic!!.mp
            origin_magic[i].power = tem_magic.power
            i++
        }


        if(num in 1..myApp.monster_num*6){

            //モンスター
            val id = num/6 + 1
            val tem_monster = realm.where(OriginMonsterModel::class.java).equalTo("id",id).findFirst()
            when(num%6){
                1 -> return tem_monster!!.hp to tem_monster.name+"のHP"
                2 -> return tem_monster!!.mp to tem_monster.name+"のMP"
                3 -> return tem_monster!!.attack to tem_monster.name+"の攻撃力"
                4 -> return tem_monster!!.defense to tem_monster.name+"の防御力"
                5 -> return tem_monster!!.speed to tem_monster.name+"の素早さ"
                0 -> return tem_monster!!.dex to tem_monster.name+"の器用さ"
            }

        }else if(num in myApp.monster_num*6+1..myApp.monster_num*6+myApp.player_num*7){

            //プレイヤー
            when((num - myApp.monster_num*6)%7){
                1 -> return origin_player!!.hp to origin_player.name+"のHP"
                2 -> return origin_player!!.mp to origin_player.name+"のMP"
                3 -> return origin_player!!.attack to origin_player.name+"の攻撃力"
                4 -> return origin_player!!.defense to origin_player.name+"の防御力"
                5 -> return origin_player!!.speed to origin_player.name+"の素早さ"
                6 -> return origin_player!!.dex to origin_player.name+"の器用さ"
                0 -> return origin_player!!.mdef to origin_player.name+"の魔法防御"
            }

        }else if(num in myApp.monster_num*6+myApp.player_num*7+1..myApp.monster_num*6+myApp.player_num*7+myApp.magic_num*2){

            //マジック
            val id = (num - myApp.monster_num*6-myApp.player_num*7)/myApp.magic_num
            when((num - myApp.monster_num*6+myApp.player_num*7)%2){
                1 -> return origin_magic[id].mp to origin_magic[id].name+"のMP"
                0 -> return origin_magic[id].power to origin_magic[id].name+"の効果"
            }

        }else if(num in myApp.monster_num*6+myApp.player_num*7+myApp.magic_num*2+1..myApp.monster_num*6+myApp.player_num*7+myApp.magic_num*2+myApp.item_num*1){

            //アイテム
            return origin_item!!.power to origin_item.name+"の効果"
        }

        return -1 to "Error"

    }
}
