package com.kato0905.shufflequest

//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.widget.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmResults
import java.security.AccessController.getContext
import kotlin.math.max
import kotlin.math.min


class Battle : Activity() {

    class Magic{
        var id: Int = 0
        var name: String =""
        var mp: Int = 0
        var power: Int = 0
    }

    var monster_id = 1
    var monster_current_hp = 1
    var monster_current_mp = 1
    var player_current_hp = 1
    var player_current_mp = 1
    var player_buf = arrayOf(0, 0, 0, 0, 0)    //attack, defense, speed, dex, mdef

    var latency = 0
    var current_progress = 0    //現在のフィールドの進行状況

    lateinit var realm: Realm
    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        findViewById<ImageView>(R.id.damageeffect1).setImageAlpha(0)
        findViewById<ImageView>(R.id.damageeffect2).setImageAlpha(0)

        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)

        realm.beginTransaction()


        //現在のフィールドの進捗状況
        current_progress = intent.getStringExtra("current_progress").toInt()
        //どの敵が出てくるか
        what_monster()
        //背景設定
        set_background()

        val magic = arrayOf(Magic(), Magic(), Magic())

        val monster = realm.where(EnemyModel::class.java).equalTo("id",monster_id).findFirst()

        val player = realm.where(PlayerModel::class.java).findFirst()

        val item = realm.where(ItemModel::class.java).equalTo("set", 1.toInt()).findFirst()

        val load_magic = realm.where(MagicModel::class.java).equalTo("canuse", 2.toInt()).findAll()

        monster_current_hp = monster!!.hp
        monster_current_mp = monster.mp

        player_current_hp = player!!.current_hp
        player_current_mp = player.current_mp

        var i = 0
        load_magic.forEach(){
            magic[i].id = it.id
            magic[i].name = it.name
            magic[i].mp = it.mp
            magic[i].power = it.power
            i++
        }

        when(item!!.id){
            6 -> player_buf[0] = item.power
            7 -> player_buf[1] = item.power
            8 -> player_buf[2] = item.power
            9 -> player_buf[4] = item.power
            else -> Log.d("system_output", "Error in player_buf")
        }
        //Toast.makeText(applicationContext, "トースト"+player.money+"メッセージ", Toast.LENGTH_LONG).show()

        //画面表示処理
        battle_view_init(monster, player, item, *magic)

        //魔法情報表示
        findViewById<TextView>(R.id.magic_info).setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(magic[0].name+" 消費MP "+magic[0].mp+" 効果 "+magic[0].power+"\n"
                            +magic[1].name+" 消費MP "+magic[1].mp+" 効果 "+magic[1].power+"\n"
                            +magic[2].name+" 消費MP "+magic[2].mp+" 効果 "+magic[2].power)
                    .setPositiveButton("確認", { dialog, which ->
                    })
                    .show()
        }

        //アイテム情報表示
        findViewById<TextView>(R.id.item_info).setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(item.explain)
                    .setPositiveButton("確認", { dialog, which ->
                    })
                    .show()
        }

        //ターン処理
        //プレイヤーの行動
        player_command_select(monster, player, item, *magic)

    }

    fun turn_func_playerTomonster(monster : EnemyModel?, player : PlayerModel, vararg magic : Magic){
        if(monster_current_hp <= 0){
            //モンスターが死んだ
            player.money += monster!!.drop
            announce(1000, monster.name+"は倒れた")
            BackToField(player)
        }
    }

    fun turn_func_monsterToplayer(monster : EnemyModel?, player : PlayerModel, vararg magic : Magic){
        if(player_current_hp <= 0){
            //プレイヤーが死んだ
            announce(1000, "力尽きた")
            BackToResult()
        }
    }

    fun turn_func_monsterTocommand(){
        if(player_current_hp <= 0){
            //プレイヤーが死んだ
            announce(1000, "力尽きた")
            BackToResult()
        }else{
            announce(1000, "コマンドを選んでください")
        }
        Handler().postDelayed(Runnable {
            clickable_true()
        }, latency.toLong())
        latency = 0
    }

    fun turn_func_playerTocommand(monster : EnemyModel?, player : PlayerModel){
        if(monster_current_hp <= 0){
            //モンスターが死んだ
            player.money += monster!!.drop
            announce(1000, monster.name+"は倒れた")
            BackToField(player)
        }else{
            announce(1000, "コマンドを選んでください")
        }
        Handler().postDelayed(Runnable {
            clickable_true()
        }, latency.toLong())
        latency = 0
    }

    fun BackToField(player : PlayerModel){
        player.current_hp = player_current_hp
        player.current_mp = player_current_mp
        realm.commitTransaction()
        realm.close()
        latency = latency + 1000
        Handler().postDelayed(Runnable {
            if(monster_id == 17){   //ゲームクリア
                val intent = Intent(this, Title::class.java)
                finish()
                startActivity(intent)
            }else if(monster_id == 17){
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content","clear")
                finish()
                startActivity(intent)
            }else {
                finish()
            }
        }, latency.toLong())
    }

    fun BackToResult(){
        realm.commitTransaction()
        realm.close()
        latency = latency + 1000
        Handler().postDelayed(Runnable {
            if(monster_id == 17){
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content", "grudge_death")
                finish()
                startActivity(intent)
            }else {
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content", "result")
                finish()
                startActivity(intent)
            }
        }, latency.toLong())
    }

    fun use_item(monster : EnemyModel?, player : PlayerModel?, item : ItemModel?){
        if(item!!.current >= 1) {
            when (item!!.id) {
                //1 薬草
                1 -> {
                    player_current_hp = max(player_current_hp + item.power, player!!.hp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(player_current_hp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のHPが" + item.power + "回復した")
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                }
                //2 聖水
                2 -> {
                    player_current_mp = max(player_current_mp + item.power, player!!.mp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のMPが" + item.power + "回復した")
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                }
                //3 上薬草
                3 -> {
                    player_current_hp = max(player_current_hp + player_current_hp * item.power, player!!.hp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(player_current_hp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のHPが" + player_current_hp * item.power + "回復した")
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                }
                //4 上聖水
                4 -> {
                    player_current_mp = max(player_current_mp + player_current_mp * item.power, player!!.mp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のMPが" + player_current_mp * item.power + "回復した")
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                }
                in 5..9 -> {
                    announce(1000, item.name+"は使うものではない!")
                }
                else -> {
                    Log.d("system_output", "Error in Item")
                }
            }
        }else{
            announce(1000, "しかし、"+item.name+"を持っていない")
        }
    }

    fun do_magic(monster : EnemyModel?, player : PlayerModel?, magic : Magic){
        if(player_current_mp >= magic.mp){
            when(magic.id) {
                //1:ファイヤ, 2:サンダー, 3:アイス
                1, 2, 3 -> {
                    var damage = if (magic.id == 1) magic.power * monster!!.fire / 100 else if (magic.id == 2) magic.power * monster!!.thunder / 100 else if (magic.id == 3) magic.power * monster!!.ice / 100 else 0
                    monster_current_hp -= damage
                    player_current_mp -= magic.mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        damage_effect_monster(monster)
                        findViewById<TextView>(R.id.monster_current_hp).setText(max(0, monster_current_hp).toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(monster!!.name + "に" + damage + "ダメージ")
                    }, latency.toLong())
                }
                //スリープ
                4 -> {
                }
                //ワープ
                5 -> {

                }
                //ライズ
                6 -> {

                }
                //ハード
                7 -> {

                }
                //ラピッド
                8 -> {

                }
                //ポイズン
                9 -> {

                }
                //ヒール
                10 -> {
                    player_current_hp = min(player_current_hp + magic.power, player!!.hp)
                    player_current_mp -= magic.mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(max(player.hp, player_current_hp).toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "は" + magic.power + "回復")
                    }, latency.toLong())
                }
                //11ブレード 12ライトニング 13バッシュ
                11, 12, 13 -> {
                    var damage = if (magic.id == 11) magic.power * player!!.attack / 100 else if (magic.id == 12) magic.power * player!!.speed / 100 else if (magic.id == 13) magic.power * player!!.defense / 100 else 0
                    monster_current_hp -= damage
                    player_current_mp -= magic.mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        damage_effect_monster(monster)
                        findViewById<TextView>(R.id.monster_current_hp).setText(max(0, monster_current_hp).toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(monster!!.name + "に" + damage + "ダメージ")
                    }, latency.toLong())
                }
                else -> {
                    Log.d("sistem_output", "Error in do_magic")
                }
            }
        }else{
            announce(1000,"しかしMPが足りない")
        }

    }

    fun player_attack(monster : EnemyModel?, player : PlayerModel?, item : ItemModel?, time : Int){
        announce(500+time, player!!.name + "の攻撃")
        var damage = (player!!.attack + player_buf[0] - monster!!.defense) * monster.resist / 100

        //クリティカルを判定
        if ((0..100).random() <= 10 * (player.dex + player_buf[3]) / monster.dex / 100) {
            announce(1000, "クリティカル")
            damage = damage * 2
        }
        //命中判定
        if ((0..100).random() <= max(30, (player.speed + player_buf[2]) / monster.speed * 100)) {
            //命中
            monster_current_hp -= max(damage, 1)//最低でも1ダメージは入る
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                findViewById<TextView>(R.id.monster_current_hp).setText(max(0, monster_current_hp).toString())
                findViewById<TextView>(R.id.announce).setText(monster.name + "に" + max(damage, 1) + "ダメージ")
                damage_effect_monster(monster)
            }, latency.toLong())
        } else {
            announce(1000, "しかし" + player.name + "の攻撃は外れた")
        }
    }

    fun player_command_select(monster : EnemyModel?, player : PlayerModel?,item : ItemModel?, vararg magic : Magic){
        //コマンドを選択
        //攻撃
        findViewById<Button>(R.id.attack_button).setOnClickListener{
            clickable_false()
            findViewById<Button>(R.id.attack_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.attack_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))

            if(player!!.speed + player_buf[2] >= monster!!.speed) {
                player_attack(monster, player, item, 0)  //プレイヤーの攻撃
                turn_func_playerTomonster(monster, player, *magic)  //プレイヤーターンからモンスターターンへ
                enemy_turn(monster, player) //モンスターターン
                turn_func_monsterTocommand()    //モンスターターンからコマンド選択へ
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, *magic)
                player_attack(monster, player, item,500)
                turn_func_playerTocommand(monster, player)
            }
        }
        findViewById<Button>(R.id.magic_button1).setOnClickListener{
            clickable_false()
            findViewById<Button>(R.id.magic_button1).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.magic_button1).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))

            if(player!!.speed + player_buf[2] >= monster!!.speed){
                announce(500, player!!.name+"の"+magic[0].name)
                do_magic(monster, player, magic[0])
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand()
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, *magic)
                announce(1000, player!!.name+"の"+magic[0].name)
                do_magic(monster, player, magic[0])
                turn_func_playerTocommand(monster, player)
            }

        }
        findViewById<Button>(R.id.magic_button2).setOnClickListener{
            clickable_false()
            findViewById<Button>(R.id.magic_button2).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.magic_button2).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))

            if(player!!.speed + player_buf[2] >= monster!!.speed){
                announce(500, player!!.name+"の"+magic[1].name)
                do_magic(monster, player, magic[1])
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand()
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, *magic)
                announce(1000, player!!.name+"の"+magic[1].name)
                do_magic(monster, player, magic[1])
                turn_func_playerTocommand(monster, player)
            }
        }
        findViewById<Button>(R.id.magic_button3).setOnClickListener{
            clickable_false()
            findViewById<Button>(R.id.magic_button3).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.magic_button3).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))

            if(player!!.speed + player_buf[2] >= monster!!.speed){
                announce(500, player!!.name+"の"+magic[2].name)
                do_magic(monster, player, magic[2])
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand()
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, *magic)
                announce(1000, player!!.name+"の"+magic[2].name)
                do_magic(monster, player, magic[2])
                turn_func_playerTocommand(monster, player)
            }
        }
        findViewById<Button>(R.id.item_button).setOnClickListener{
            findViewById<Button>(R.id.item_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.item_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))

            announce(500, player!!.name+"は"+item!!.name+"を使用した")
            use_item(monster, player, item)
            turn_func_playerTomonster(monster, player, *magic)
            enemy_turn(monster, player)
            turn_func_monsterTocommand()
        }
        findViewById<Button>(R.id.escape_button).setOnClickListener{
            clickable_false()
            findViewById<Button>(R.id.escape_button).alpha = 0.6.toFloat()   //ボタンを透明に
            Handler().postDelayed(Runnable {
                findViewById<Button>(R.id.escape_button).alpha = 1.toFloat()   //ボタンを非透明に
            }, (100.toLong()))

            announce(500, player!!.name+"は逃げ出そうとした")
            if((0..100).random() <= max(30, 40*(player!!.speed+player_buf[2])/monster!!.speed)){
                //逃走成功
                announce(1000, "うまく逃げ切れた")
                player.current_hp = player_current_hp
                player.current_mp = player_current_mp
                BackToField(player)
            }else{
                announce(1000, "しかし回り込まれた")
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand()
            }
        }
    }

    fun enemy_turn(monster : EnemyModel?, player : PlayerModel?){
        when(monster!!.chara){
            1 -> {
                //直接攻撃のみ
                monster_direct_attack(monster, player)
            }
            else -> {
                Log.d("sistem_output", "Error in enemy_turn")
            }
        }
    }

    fun monster_direct_attack(monster : EnemyModel?, player : PlayerModel?){
        announce(1000, monster!!.name+"の攻撃")
        var damage = monster!!.attack - player!!.defense + player_buf[1]

        //クリティカル判定
        if((0..100).random() <= 10 * monster.dex/(player.dex+player_buf[3])){
            announce(1000,"クリティカル")
            damage = damage * 2
        }

        //回避判定
        if((0..100).random() >= max(2,(player.speed+player_buf[2])/monster.speed)){
            //命中
            latency = latency + 1000
            player_current_hp -= max(damage, 1)//最低でも1ダメージは入る
            Handler().postDelayed(Runnable {
                damage_effect_player(player)
                findViewById<TextView>(R.id.player_current_hp).setText(max(0,player_current_hp).toString())
                findViewById<TextView>(R.id.announce).setText(player.name+"に"+max(damage, 1)+"ダメージ")
            }, latency.toLong())
        }else{
            announce(1000, "しかし"+monster!!.name+"の攻撃は外れた")
        }
    }

    fun damage_effect_monster(monster : EnemyModel?){
        //Toast.makeText(applicationContext, "トースト"+"メッセージ", Toast.LENGTH_LONG).show()
        for(i in 0..2){
            Handler().postDelayed(Runnable {
                findViewById<ImageView>(R.id.monster_view).setImageAlpha(0)   //透明
            }, ((i*200).toLong()))

            Handler().postDelayed(Runnable {
                findViewById<ImageView>(R.id.monster_view).setImageAlpha(255)   //非透明
            }, ((i*200)+100.toLong()))
        }
    }

    fun damage_effect_player(player : PlayerModel?){
        findViewById<ImageView>(R.id.damageeffect1).setImageAlpha(255)
        Handler().postDelayed(Runnable {
            findViewById<ImageView>(R.id.damageeffect1).setImageAlpha(255)   //非透明
        }, 0)

        Handler().postDelayed(Runnable {
            findViewById<ImageView>(R.id.damageeffect1).setImageAlpha(0)   //透明
        }, 100)

        Handler().postDelayed(Runnable {
            findViewById<ImageView>(R.id.damageeffect2).setImageAlpha(255)   //非透明
        }, 200)

        Handler().postDelayed(Runnable {
            findViewById<ImageView>(R.id.damageeffect2).setImageAlpha(0)   //透明
        }, 300)
    }

    fun clickable_false(){
        findViewById<Button>(R.id.attack_button).isClickable = false
        findViewById<Button>(R.id.magic_button1).isClickable = false
        findViewById<Button>(R.id.magic_button2).isClickable = false
        findViewById<Button>(R.id.magic_button3).isClickable = false
        findViewById<Button>(R.id.item_button).isClickable = false
        findViewById<Button>(R.id.escape_button).isClickable = false
    }

    fun clickable_true(){
        findViewById<Button>(R.id.attack_button).isClickable = true
        findViewById<Button>(R.id.magic_button1).isClickable = true
        findViewById<Button>(R.id.magic_button2).isClickable = true
        findViewById<Button>(R.id.magic_button3).isClickable = true
        findViewById<Button>(R.id.item_button).isClickable = true
        findViewById<Button>(R.id.escape_button).isClickable = true
    }

    fun announce(late : Int, announce : String){
        latency = latency + late
        Handler().postDelayed(Runnable {
            findViewById<TextView>(R.id.announce).setText(announce)
        }, latency.toLong())
    }

    fun what_monster(){
        var random = (0..100).random()
        //Toast.makeText(applicationContext, "トースト"+random+"メッセージ", Toast.LENGTH_LONG).show()
        when(current_progress){
            in 0..15 -> {
                monster_id = if(random <= 50) 1 else 2
            }
            in 16..30 -> {
                monster_id = if(random <= 20) 3 else if(random > 20 && random <= 60) 4 else 5
            }
            in 31..60 -> {
                monster_id = if(random <= 20) 6 else if(random > 20 && random <= 60) 7 else 8
            }
            in 61..80 -> {
                monster_id = if(random <= 20) 9 else if(random > 20 && random <= 60) 10 else 11
            }
            in 81..99 -> {
                monster_id = if(random <= 20) 12 else if(random > 20 && random <= 60) 13 else 14
            }
            100 -> monster_id = 16
            101 -> monster_id = 17
            else -> {
                Log.d("sistem_output", "Error in what_monster")
            }
        }
    }

    fun set_background(){
        //
        //0~15      grass
        //16~30     forest
        //31~60     wasteland
        //61~80     dangeon
        //81~99    devilcastle
        //100       vsdevil
        //101       intro
        //
        when(current_progress){
            in 0..15 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_grass)
            in 16..30 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_forest)
            in 31..60 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_wasteland)
            in 61..80 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_dangeon)
            in 81..99 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_devilcastle)
            100 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_vsdevil)
            101 -> findViewById<ImageView>(R.id.background).setImageResource(R.drawable.background_intro)
            else -> Log.d("sistem_output", "Error in set_background")
        }
    }

    fun battle_view_init(monster : EnemyModel?, player : PlayerModel?, item : ItemModel?, vararg magic : Magic){
        //アナウンス
        findViewById<TextView>(R.id.announce).setText(monster!!.name+"があらわれた")

        //モンスターステータス表示
        findViewById<TextView>(R.id.monster_name).setText(monster.name)
        findViewById<TextView>(R.id.monster_current_hp).setText(monster_current_hp.toString())
        findViewById<TextView>(R.id.monster_current_mp).setText(monster_current_mp.toString())
        findViewById<TextView>(R.id.monster_attack).setText(monster.attack.toString())
        findViewById<TextView>(R.id.monster_defense).setText(monster.defense.toString())
        findViewById<TextView>(R.id.monster_speed).setText(monster.speed.toString())
        findViewById<TextView>(R.id.monster_dex).setText(monster.dex.toString())
        findViewById<TextView>(R.id.monster_resist).setText(monster.resist.toString())
        findViewById<TextView>(R.id.monster_fire).setText(monster.fire.toString())
        findViewById<TextView>(R.id.monster_thunder).setText(monster.thunder.toString())
        findViewById<TextView>(R.id.monster_ice).setText(monster.ice.toString())
        findViewById<TextView>(R.id.monster_poison).setText(monster.poison.toString())

        //プレイヤーステータス表示
        findViewById<TextView>(R.id.player_name).setText(player!!.name)
        findViewById<TextView>(R.id.player_current_hp).setText(player_current_hp.toString())
        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
        findViewById<TextView>(R.id.player_maxhp).setText(" / "+player.hp.toString())
        findViewById<TextView>(R.id.player_maxmp).setText(" / "+player.mp.toString())
        findViewById<TextView>(R.id.player_attack).setText(player.attack.toString())
        findViewById<TextView>(R.id.player_defense).setText(player.defense.toString())
        findViewById<TextView>(R.id.player_mdef).setText(player.mdef.toString())
        findViewById<TextView>(R.id.player_speed).setText(player.speed.toString())
        findViewById<TextView>(R.id.player_dex).setText(player.dex.toString())

        //魔法名前表示
        findViewById<Button>(R.id.magic_button1).setText(magic[0].name)
        findViewById<Button>(R.id.magic_button2).setText(magic[1].name)
        findViewById<Button>(R.id.magic_button3).setText(magic[2].name)

        //アイテム名前表示
        findViewById<Button>(R.id.item_button).setText(item!!.name)

        //モンスター画像表示
        val monsterview = getResources().getIdentifier(monster.imagename, "drawable", "com.kato0905.shufflequest")
        findViewById<ImageView>(R.id.monster_view).setImageResource(monsterview)
    }

}
