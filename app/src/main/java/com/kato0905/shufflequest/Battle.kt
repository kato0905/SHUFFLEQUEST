package com.kato0905.shufflequest

//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
//import android.support.constraint.ConstraintLayout
import android.util.Log
import android.widget.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmResults
import java.security.AccessController.getContext
import java.util.*
import kotlin.math.max
import kotlin.math.min
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer


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
    var monster_condition = 0   //1: poison, 2: sleep
    var monster_condition_power = 0 //毒の威力or眠りの解除確率(25,50,75,100)
    var player_condition = 0
    var player_condition_power = 0
    var player_buf = arrayOf(0, 0, 0, 0, 0)    //attack, defense, speed, dex, mdef
    private lateinit var soundPool: SoundPool
    private lateinit var battle_bgm: MediaPlayer

    var latency = 0
    var endflag = 0
    var end_solid_flag = 0
    var current_progress = 0    //現在のフィールドの進行状況

    companion object {
        //se
        var se_monster_attack = 0
        var se_monster_critical = 0
        var se_player_attack = 0
        var se_player_critical = 0
        var se_fire = 0
        var se_ice = 0
        var se_thunder = 0
        var se_monster_impact = 0
        var se_monster_fire = 0
        var se_monster_death = 0
        var se_monster_secondattack = 0
        var se_monster_wood = 0
        var se_poison = 0
        var se_blade = 0
        var se_worp = 0
        var se_sleep = 0
        var se_heal = 0
        var se_escape = 0
        var se_miss = 0
        var se_monster_defeat = 0
        var se_player_dead = 0
        var se_get_drop = 0

    }


    lateinit var realm: Realm
    private val mHandler = Handler()

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

        battle_bgm = MediaPlayer.create(this, R.raw.battle_bgm)
        battle_bgm.setVolume(0.5f, 0.5f)
        battle_bgm.setLooping(true)
        battle_bgm.start()

        loadSoundIDs(this)

    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        battle_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        findViewById<ImageView>(R.id.damageeffect1).setImageAlpha(0)
        findViewById<ImageView>(R.id.damageeffect2).setImageAlpha(0)

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

        //アイテムによる攻撃力上昇
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
                    .setMessage(item.explain+" 効果 : "+item.power+"("+item.current+"個)")
                    .setPositiveButton("確認", { dialog, which ->
                    })
                    .show()
        }

        //ターン処理
        //プレイヤーの行動
        player_command_select(monster, player, item, *magic)

    }

    private fun loadSoundIDs(context:Context) {
        soundPool?.let {
            se_monster_attack = it.load(this, R.raw.monster_attack, 1)
            se_monster_critical = it.load(this, R.raw.monster_critical, 1)
            se_player_attack = it.load(this, R.raw.player_attack, 1)
            se_player_critical = it.load(this, R.raw.player_critical, 1)
            se_fire = it.load(this, R.raw.fire, 1)
            se_ice = it.load(this, R.raw.ice, 1)
            se_thunder = it.load(this, R.raw.thunder, 1)
            se_monster_impact = it.load(this, R.raw.monster_impact, 1)
            se_monster_fire = it.load(this, R.raw.monster_fire, 1)
            se_monster_death = it.load(this, R.raw.monster_death, 1)
            se_monster_secondattack = it.load(this, R.raw.monster_secondattack, 1)
            se_monster_wood = it.load(this, R.raw.monster_wood, 1)
            se_poison = it.load(this, R.raw.poison, 1)
            se_blade = it.load(this, R.raw.blade, 1)
            se_worp = it.load(this, R.raw.worp, 1)
            se_sleep = it.load(this, R.raw.sleep, 1)
            se_heal = it.load(this, R.raw.heal, 1)
            se_escape = it.load(this, R.raw.escape, 1)
            se_miss = it.load(this, R.raw.miss,1)
            se_monster_defeat = it.load(this, R.raw.monster_defeat,1)
            se_player_dead = it.load(this, R.raw.player_dead,1)
            se_get_drop = it.load(this, R.raw.get_drop,1)
        }
    }


    fun turn_func_monsterToplayer(monster : EnemyModel?, player : PlayerModel,item : ItemModel?, vararg magic : Magic){
        if(player_current_hp <= 0){
            //プレイヤーが死んだ
            player_death(monster, player, item)
        }
    }

    fun turn_func_monsterTocommand(monster : EnemyModel?, player : PlayerModel, item :ItemModel?){
        if(player_current_hp <= 0){
            //プレイヤーが死んだ
            player_death(monster, player, item)
        }else{
            if(monster_condition == 1){
                monster_current_hp -= monster_condition_power
                val display = monster_current_hp
                latency = latency + 1000
                Handler().postDelayed(Runnable {
                    damage_effect_monster(monster)
                    findViewById<TextView>(R.id.monster_current_hp).setText(display.toString())
                    findViewById<TextView>(R.id.announce).setText(monster!!.name+"は毒により"+monster_condition_power+"ダメージ受けた")
                    soundPool.play(se_poison, 1.0f, 1.0f, 1, 0, 1.0f)
                }, latency.toLong())
            }
            if(monster_current_hp <= 0) {
                monster_death(monster, player)
            }else{
                announce(1000, "コマンドを選んでください")
            }
        }
        Handler().postDelayed(Runnable {
            clickable_true()
        }, latency.toLong())
        latency = 0
    }

    fun player_death(monster: EnemyModel?, player: PlayerModel?, item: ItemModel?){
        if(endflag == 0) {
            if(item!!.id == 5 && item.current >= 1){
                player_current_hp = min(item.power, player!!.hp)
                val display = player_current_hp
                latency = latency + 1000
                Handler().postDelayed(Runnable {
                    findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                    findViewById<TextView>(R.id.announce).setText(item.name+"の効果により、HPを"+display+"で復活")
                }, latency.toLong())
                item.current--
                realm.commitTransaction()
                realm.beginTransaction()
            }else {
                endflag = 1
                latency = latency + 1000
                Handler().postDelayed(Runnable {
                    soundPool.play(se_player_dead, 1.0f, 1.0f, 1, 0, 1.0f)
                    findViewById<TextView>(R.id.announce).setText(player!!.name+"は力尽きた")
                }, latency.toLong())
                BackToResult()
            }
        }
    }

    fun turn_func_playerTomonster(monster : EnemyModel?, player : PlayerModel, vararg magic : Magic){
        if(monster_current_hp <= 0){
            monster_death(monster, player)
        }
    }

    fun turn_func_playerTocommand(monster : EnemyModel?, player : PlayerModel){
        if(monster_current_hp <= 0){
            monster_death(monster, player)
        }else{
            if(monster_condition == 1){
                monster_current_hp -= monster_condition_power
                val display = monster_current_hp
                latency = latency + 1000
                Handler().postDelayed(Runnable {
                    damage_effect_monster(monster)
                    findViewById<TextView>(R.id.monster_current_hp).setText(display.toString())
                    findViewById<TextView>(R.id.announce).setText(monster!!.name+"は毒により"+monster_condition_power+"ダメージ受けた")
                    soundPool.play(se_poison, 1.0f, 1.0f, 1, 0, 1.0f)
                }, latency.toLong())
            }
            if(monster_current_hp <= 0) {
                monster_death(monster, player)
            }else{
                announce(1000, "コマンドを選んでください")
            }
        }

        Handler().postDelayed(Runnable {
            clickable_true()
        }, latency.toLong())
        latency = 0
    }

    fun monster_death(monster : EnemyModel?, player : PlayerModel){
        if(monster!!.chara == 5 && end_solid_flag == 0) {
            monster_current_hp = 1
            val display = monster_current_hp
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                findViewById<TextView>(R.id.monster_current_hp).setText(display.toString())
                findViewById<TextView>(R.id.announce).setText("しかし、"+monster.name + "はHP"+display+"で耐えた")
            }, latency.toLong())
            end_solid_flag = 1
        }else {
            //モンスターが死んだ
            if (endflag == 0) {
                endflag = 1
                player.money += monster!!.drop
                latency = latency + 1000
                Handler().postDelayed(Runnable {
                    soundPool.play(se_monster_defeat, 1.0f, 1.0f, 1, 0, 1.0f)
                    findViewById<TextView>(R.id.announce).setText(monster.name + "は倒れた")
                }, latency.toLong())
                latency = latency + 1000
                Handler().postDelayed(Runnable {
                    soundPool.play(se_get_drop, 1.0f, 1.0f, 1, 0, 1.0f)
                    findViewById<TextView>(R.id.announce).setText(player.name+"は"+monster.drop+"円手に入れた")
                }, latency.toLong())
                BackToField(player)
            }
        }
    }

    fun BackToField(player : PlayerModel){
        player.current_hp = player_current_hp
        player.current_mp = player_current_mp
        latency = latency + 1000
        Handler().postDelayed(Runnable {
            if(monster_id == 17 && monster_current_hp <= 0){   //ゲームクリア
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content","true_clear")
                soundPool?.release()
                battle_bgm.release()
                finish()
                startActivity(intent)
            }else if(monster_id == 16 && monster_current_hp <= 0){ //魔王を倒した
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content","clear")
                soundPool?.release()
                battle_bgm.release()
                finish()
                startActivity(intent)
            }else {
                soundPool?.release()
                battle_bgm.release()
                finish()
            }
            realm.commitTransaction()
            realm.close()
        }, latency.toLong())
    }

    fun BackToResult(){

        latency = latency + 1000
        Handler().postDelayed(Runnable {
            if(monster_id == 17){
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content", "grudge_death")
                soundPool?.release()
                battle_bgm.release()
                finish()
                startActivity(intent)
            }else {
                val intent = Intent(this, Intro::class.java)
                intent.putExtra("content", "result")
                soundPool?.release()
                battle_bgm.release()
                finish()
                startActivity(intent)
            }
            realm.commitTransaction()
            realm.close()
        }, latency.toLong())
    }


    fun use_item(monster : EnemyModel?, player : PlayerModel?, item : ItemModel?){
        if(item!!.current >= 1) {
            when (item!!.id) {
                //1 薬草
                1 -> {
                    player_current_hp = min(player_current_hp + item.power, player!!.hp)
                    val display = min(player_current_hp + item.power, player!!.hp)
                    latency = latency + 1000

                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のHPが" + item.power + "回復した")
                        soundPool.play(se_heal, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                    realm.beginTransaction()
                }
                //2 聖水
                2 -> {
                    player_current_mp = min(player_current_mp + item.power, player!!.mp)
                    val display = min(player_current_mp + item.power, player!!.mp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のMPが" + item.power + "回復した")
                        soundPool.play(se_heal, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                    realm.beginTransaction()
                }
                //3 上薬草
                3 -> {
                    var recovery_amount = player!!.hp * item.power / 100
                    player_current_hp = min(player_current_hp + recovery_amount, player!!.hp)
                    val display = min(player_current_hp + recovery_amount, player!!.hp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のHPが" + recovery_amount + "回復した")
                        soundPool.play(se_heal, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                    realm.beginTransaction()
                }
                //4 上聖水
                4 -> {
                    var recovery_amount = player!!.mp * item.power / 100
                    player_current_mp = min(player_current_mp + recovery_amount, player!!.mp)
                    var display = min(player_current_mp + recovery_amount, player!!.mp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "のMPが" + recovery_amount + "回復した")
                        soundPool.play(se_heal, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                    item.current--
                    realm.commitTransaction()
                    realm.beginTransaction()
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
                    val display = max(0, monster_current_hp)
                    val display2 = player_current_mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        damage_effect_monster(monster)
                        findViewById<TextView>(R.id.monster_current_hp).setText(display.toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(display2.toString())
                        findViewById<TextView>(R.id.announce).setText(monster!!.name + "に" + damage + "ダメージ")
                        when(magic.id){
                            1 -> soundPool.play(se_fire, 1.0f, 1.0f, 1, 0, 1.0f)
                            2 -> soundPool.play(se_thunder, 1.0f, 1.0f, 1, 0, 1.0f)
                            3 -> soundPool.play(se_ice, 1.0f, 1.0f, 1, 0, 1.0f)
                        }
                    }, latency.toLong())
                }
                //スリープ
                4 -> {
                    player_current_mp -= magic.mp
                    val display = player_current_mp

                    if((0..100).random() <= monster!!.poison + magic.power){
                        monster_condition = 2
                        monster_condition_power = (0..4).random()
                        latency = latency + 1000
                        Handler().postDelayed(Runnable {
                            findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                            findViewById<TextView>(R.id.announce).setText(monster.name+"は眠りについた")
                            findViewById<TextView>(R.id.monster_condition).setText("眠り")
                            soundPool.play(se_sleep, 1.0f, 1.0f, 1, 0, 1.0f)
                        }, latency.toLong())
                    }else{
                        latency = latency + 1000
                        Handler().postDelayed(Runnable {
                            findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                            findViewById<TextView>(R.id.announce).setText("しかし、"+monster.name+"は眠気に抵抗した")
                        }, latency.toLong())
                    }
                }
                //ワープ
                5 -> {
                    player_current_mp -= magic.mp
                    val display = player_current_mp
                    player!!.current_hp = player_current_hp
                    player.current_mp = player_current_mp
                    announce(1000, player.name + "は街までワープした")
                    latency = latency + 1000
                    if(endflag == 0){
                        endflag = 1

                        Handler().postDelayed(Runnable {
                            val intent = Intent(this, Map_town::class.java)
                            soundPool?.release()
                            battle_bgm.release()
                            finish()
                            startActivity(intent)
                            soundPool.play(se_worp, 1.0f, 1.0f, 1, 0, 1.0f)
                            realm.commitTransaction()
                            realm.close()
                        }, latency.toLong())
                    }
                }
                //ライズ
                6 -> {
                    player_current_mp -= magic.mp
                    player_buf[0] += player!!.attack * magic.power / 100
                    val display = player_current_mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player!!.name+"の攻撃力が上がった")
                    }, latency.toLong())
                }
                //ハード
                7 -> {
                    player_current_mp -= magic.mp
                    player_buf[1] += player!!.defense * magic.power / 100
                    val display = player_current_mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name+"の防御力が上がった")
                    }, latency.toLong())
                }
                //ラピッド
                8 -> {
                    player_current_mp -= magic.mp
                    player_buf[2] += player!!.attack * magic.power / 100
                    val display = player_current_mp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name+"の素早さが上がった")
                    }, latency.toLong())
                }
                //ポイズン
                9 -> {
                    player_current_mp -= magic.mp
                    val display = player_current_mp

                    if((0..100).random() <= monster!!.poison){
                        monster_condition = 1
                        monster_condition_power = magic.power
                        latency = latency + 1000
                        Handler().postDelayed(Runnable {
                            findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                            findViewById<TextView>(R.id.announce).setText(monster.name+"は毒に侵された")
                            findViewById<TextView>(R.id.monster_condition).setText("毒")
                            soundPool.play(se_poison, 1.0f, 1.0f, 1, 0, 1.0f)
                        }, latency.toLong())
                    }else{
                        latency = latency + 1000
                        Handler().postDelayed(Runnable {
                            findViewById<TextView>(R.id.player_current_mp).setText(display.toString())
                            findViewById<TextView>(R.id.announce).setText("しかし、"+monster.name+"は毒に抵抗した")
                        }, latency.toLong())
                    }
                }
                //ヒール
                10 -> {
                    player_current_hp = min(player_current_hp + magic.power, player!!.hp)
                    player_current_mp -= magic.mp
                    val display = player_current_hp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "は" + magic.power + "回復")
                        soundPool.play(se_heal, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                }
                //11ブレード 12ライトニング 13バッシュ
                11, 12, 13 -> {
                    var damage = if (magic.id == 11) magic.power * player!!.attack / 100 else if (magic.id == 12) magic.power * player!!.speed / 100 else if (magic.id == 13) magic.power * player!!.defense / 100 else 0
                    monster_current_hp -= damage
                    player_current_mp -= magic.mp
                    val display = max(0, monster_current_hp)
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        damage_effect_monster(monster)
                        findViewById<TextView>(R.id.monster_current_hp).setText(display.toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(monster!!.name + "に" + damage + "ダメージ")
                        soundPool.play(se_blade, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                }
                14 -> {
                    player_current_hp = min(player_current_hp + player!!.hp * magic.power / 100, player!!.hp)
                    player_current_mp -= magic.mp
                    val display = player_current_hp
                    latency = latency + 1000
                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                        findViewById<TextView>(R.id.player_current_mp).setText(player_current_mp.toString())
                        findViewById<TextView>(R.id.announce).setText(player.name + "は" + player!!.hp * magic.power / 100 + "回復")
                        soundPool.play(se_heal, 1.0f, 1.0f, 1, 0, 1.0f)
                    }, latency.toLong())
                }
                else -> {
                    Log.d("sistem_output", "Error in do_magic")
                }
            }
        }else{
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                soundPool.play(se_miss, 1.0f, 1.0f, 1, 0, 1.0f)
                findViewById<TextView>(R.id.announce).setText("しかしMPが足りない")
            }, latency.toLong())
        }
    }

    fun player_attack(monster : EnemyModel?, player : PlayerModel?, item : ItemModel?, time : Int){
        announce(500+time, player!!.name + "の攻撃")
        var damage = (player!!.attack + player_buf[0] - monster!!.defense) * monster.resist / 100

        //命中判定
        if ((0..100).random() <= max(30, (player.speed + player_buf[2]) / monster.speed * 100)) {
            //命中
            //クリティカルを判定
            if ((0..100).random() <= 10 * (player.dex + player_buf[3]) / monster.dex / 100) {
                announce(1000, "クリティカル")
                damage = damage * 2
            }
            monster_current_hp -= max(damage, 1)//最低でも1ダメージは入る
            val display = max(0, monster_current_hp)
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                findViewById<TextView>(R.id.monster_current_hp).setText(display.toString())
                findViewById<TextView>(R.id.announce).setText(monster.name + "に" + max(damage, 1) + "ダメージ")
                damage_effect_monster(monster)
                if(damage >= monster.hp/3) {
                    soundPool.play(se_player_critical, 1.0f, 1.0f, 1, 0, 1.0f)
                }else{
                    soundPool.play(se_player_attack, 1.0f, 1.0f, 1, 0, 1.0f)
                }
            }, latency.toLong())
        } else {
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                soundPool.play(se_miss, 1.0f, 1.0f, 1, 0, 1.0f)
                findViewById<TextView>(R.id.announce).setText("しかし" + player.name + "の攻撃は外れた")
            }, latency.toLong())
        }
    }

    fun player_command_select(monster : EnemyModel?, player : PlayerModel?,item : ItemModel?, vararg magic : Magic){
        //コマンドを選択
        //攻撃
        findViewById<Button>(R.id.attack_button).setOnClickListener{
            clickable_false()
            button_click_effect("attack_button")

            if(player!!.speed + player_buf[2] >= monster!!.speed) {
                player_attack(monster, player, item, 0)  //プレイヤーの攻撃
                turn_func_playerTomonster(monster, player, *magic)  //プレイヤーターンからモンスターターンへ
                enemy_turn(monster, player) //モンスターターン
                turn_func_monsterTocommand(monster, player, item)    //モンスターターンからコマンド選択へ
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, item, *magic)
                player_attack(monster, player, item,500)
                turn_func_playerTocommand(monster, player)
            }
        }
        findViewById<Button>(R.id.magic_button1).setOnClickListener{
            clickable_false()
            button_click_effect("magic_button1")

            if(player!!.speed + player_buf[2] >= monster!!.speed){
                announce(500, player!!.name+"の"+magic[0].name)
                do_magic(monster, player, magic[0])
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand(monster, player, item)
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, item, *magic)
                announce(1000, player!!.name+"の"+magic[0].name)
                do_magic(monster, player, magic[0])
                turn_func_playerTocommand(monster, player)
            }

        }
        findViewById<Button>(R.id.magic_button2).setOnClickListener{
            clickable_false()
            button_click_effect("magic_button2")

            if(player!!.speed + player_buf[2] >= monster!!.speed){
                announce(500, player!!.name+"の"+magic[1].name)
                do_magic(monster, player, magic[1])
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand(monster, player, item)
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, item, *magic)
                announce(1000, player!!.name+"の"+magic[1].name)
                do_magic(monster, player, magic[1])
                turn_func_playerTocommand(monster, player)
            }
        }
        findViewById<Button>(R.id.magic_button3).setOnClickListener{
            clickable_false()
            button_click_effect("magic_button3")

            if(player!!.speed + player_buf[2] >= monster!!.speed){
                announce(500, player!!.name+"の"+magic[2].name)
                do_magic(monster, player, magic[2])
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand(monster, player, item)
            }else{
                enemy_turn(monster, player)
                turn_func_monsterToplayer(monster, player, item, *magic)
                announce(1000, player!!.name+"の"+magic[2].name)
                do_magic(monster, player, magic[2])
                turn_func_playerTocommand(monster, player)
            }
        }
        findViewById<Button>(R.id.item_button).setOnClickListener{
            clickable_false()
            button_click_effect("item_button")

            announce(500, player!!.name+"は"+item!!.name+"を使用した")
            use_item(monster, player, item)
            turn_func_playerTomonster(monster, player, *magic)
            enemy_turn(monster, player)
            turn_func_monsterTocommand(monster, player, item)
        }
        findViewById<Button>(R.id.escape_button).setOnClickListener{
            clickable_false()
            button_click_effect("escape_button")

            announce(500, player!!.name+"は逃げ出そうとした")
            if((0..100).random() <= max(30, 40*(player!!.speed+player_buf[2])/monster!!.speed)){
                //逃走成功
                latency += 1000
                Handler().postDelayed(Runnable {
                    soundPool.play(se_escape, 1.0f, 1.0f, 1, 0, 1.0f)
                    findViewById<TextView>(R.id.announce).setText("うまく逃げ切れた")
                }, latency.toLong())
                player.current_hp = player_current_hp
                player.current_mp = player_current_mp
                BackToField(player)
            }else{
                announce(1000, "しかし回り込まれた")
                turn_func_playerTomonster(monster, player!!, *magic)
                enemy_turn(monster, player)
                turn_func_monsterTocommand(monster, player, item)
            }
        }
    }

    fun button_click_effect(id_name : String){
        val button_id = getResources().getIdentifier(id_name, "id", "com.kato0905.shufflequest")
        findViewById<Button>(button_id).alpha = 0.6.toFloat()   //ボタンを透明に
        Handler().postDelayed(Runnable {
            findViewById<Button>(button_id).alpha = 1.toFloat()   //ボタンを非透明に
        }, (100.toLong()))
    }

    fun enemy_turn(monster : EnemyModel?, player : PlayerModel?){
        if(monster_condition == 2){
            monster_condition_power--
            if(monster_condition_power <= 0){
                monster_condition = 0
                latency += 1000
                Handler().postDelayed(Runnable {
                    findViewById<TextView>(R.id.monster_condition).setText("")
                    findViewById<TextView>(R.id.announce).setText(monster!!.name+"は飛び起きた")
                }, latency.toLong())
            }else{
                latency += 1000
                Handler().postDelayed(Runnable {
                    soundPool.play(se_sleep, 1.0f, 1.0f, 1, 0, 1.0f)
                    findViewById<TextView>(R.id.announce).setText(monster!!.name+"は眠っている")
                }, latency.toLong())
                return
            }
        }

        when(monster!!.chara){
            1,5,7,8 -> {
                //直接攻撃のみ
                monster_direct_attack(monster, player)
            }
            2,3,4 -> {
                //直接攻撃＋魔法
                //2 直接攻撃/魔法の確率：70/30
                //3 直接攻撃/魔法の確率：50/50
                //4 直接攻撃/魔法の確率：10/90
                //
                //MPがない場合、10%で魔法を打とうとして失敗する。90%で直接攻撃
                if (monster_current_mp < monster.mp/3) {
                    if ((0..100).random() <= 10) {
                        monster_magic(monster, player)
                    } else {
                        monster_direct_attack(monster, player)
                    }
                } else {
                    if ((0..100).random() <= 30 && monster.chara == 2) {
                        monster_magic(monster, player)
                    } else if ((0..100).random() <= 50 && monster.chara == 3) {
                        monster_magic(monster, player)
                    } else if ((0..100).random() <= 90 && monster.chara == 4) {
                        monster_magic(monster, player)
                    } else {
                        monster_direct_attack(monster, player)
                    }
                }
            }
            6 -> {
                //確率で二回攻撃
                monster_direct_attack(monster, player)
                if((0..100).random() <= 70){
                    announce(1000, monster.name+"の二回攻撃")
                    monster_direct_attack(monster, player)
                }
            }
            9 -> {
                /*
                ・MPがまだあるとき
                一回攻撃 20%
                二回攻撃 40%
                ファイヤ 40%
                ・MPがないとき
                一回攻撃 20%
                二回攻撃 70%
                ファイヤ 10%
                */
                if(monster_current_mp < monster.mp/3){
                    when((0..100).random()){
                        in 0..20 -> monster_direct_attack(monster, player)
                        in 21..60 -> {
                            monster_direct_attack(monster, player)
                            announce(1000, monster.name+"の二回攻撃")
                            monster_direct_attack(monster, player)
                        }
                        in 61..100 -> monster_magic(monster, player)
                    }
                }else{
                    when((0..100).random()) {
                        in 0..20 -> monster_direct_attack(monster, player)
                        in 21..90 -> {
                            monster_direct_attack(monster, player)
                            announce(1000, monster.name + "の二回攻撃")
                            monster_direct_attack(monster, player)
                        }
                        in 91..100 -> monster_magic(monster, player)
                    }
                }

            }
            else -> {
                Log.d("sistem_output", "Error in enemy_turn")
            }
        }
    }

    fun monster_magic(monster : EnemyModel?, player : PlayerModel?){
        var use_mp = 0
        var damage = 0

        when(monster!!.chara){
            2 -> {
                announce(1000, monster!!.name+"のウッド")
                damage = monster.mp/4
                use_mp = monster.mp/2
            }
            3 -> {
                announce(1000, monster!!.name+"のインパクト")
                damage = monster.mp/3
                use_mp = monster.mp/3
            }
            4 -> {
                announce(1000, monster!!.name+"のデス")
                damage = monster.mp/2
                use_mp = monster.mp/3
            }
            9 -> {
                announce(1000, monster!!.name+"のインフェルノ")
                damage = monster.mp
                use_mp = monster.mp/3
            }
            else -> {
                Log.d("system_output","Error in Battle.kt fun monster_magic")
            }
        }
        damage = max(0, (100 - player!!.mdef) * damage / 100)


        if(monster_current_mp >= use_mp){
            monster_current_mp -= use_mp
            player_current_hp -= max(damage, 0)
            latency = latency + 1000
            val display = max(0,player_current_hp)
            Handler().postDelayed(Runnable {
                damage_effect_player(player)
                findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                findViewById<TextView>(R.id.monster_current_mp).setText(monster_current_mp.toString())
                findViewById<TextView>(R.id.announce).setText(player!!.name+"に"+max(damage, 0)+"ダメージ")
                when(monster.chara){
                    2 -> soundPool.play(se_monster_wood, 1.0f, 1.0f, 1, 0, 1.0f)
                    3 -> soundPool.play(se_monster_impact, 1.0f, 1.0f, 1, 0, 1.0f)
                    4 -> soundPool.play(se_monster_death, 1.0f, 1.0f, 1, 0, 1.0f)
                    9 -> soundPool.play(se_monster_fire, 1.0f, 1.0f, 1, 0, 1.0f)
                }
            }, latency.toLong())
        }else{
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                soundPool.play(se_miss, 1.0f, 1.0f, 1, 0, 1.0f)
                findViewById<TextView>(R.id.announce).setText("しかし、"+monster.name+"のMPが足りない")
            }, latency.toLong())
        }
    }


    fun monster_direct_attack(monster : EnemyModel?, player : PlayerModel?){
        announce(1000, monster!!.name+"の攻撃")

        var monster_attack = monster.attack
        if(monster.chara == 7 && monster_current_hp <= monster.hp / 2){
            monster_attack = monster.attack * 3
        }else{
            monster_attack = monster.attack
        }
        var damage = monster_attack - player!!.defense - player_buf[1]


        //回避判定
        if((0..100).random() >= max(2,(player.speed+player_buf[2])/monster.speed)){
            //命中
            //クリティカル判定
            if((0..100).random() <= 10 * monster.dex/(player.dex+player_buf[3])){
                announce(1000,"クリティカル")
                damage = damage * 2
            }
            latency = latency + 1000
            if(monster.chara == 8 && damage <= player.hp / 3){
                player_current_hp -= max(player.hp/3,1)
                val display = max(0, player_current_hp)
                Handler().postDelayed(Runnable {
                    damage_effect_player(player)
                    findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                    findViewById<TextView>(R.id.announce).setText(player.name+"に"+max(player.hp/3, 1)+"ダメージ")
                    soundPool.play(se_monster_critical, 1.0f, 1.0f, 1, 0, 1.0f)
                }, latency.toLong())
            }else {
                player_current_hp -= max(damage, 1)//最低でも1ダメージは入る
                val display = max(0,player_current_hp)
                Handler().postDelayed(Runnable {
                    damage_effect_player(player)
                    findViewById<TextView>(R.id.player_current_hp).setText(display.toString())
                    findViewById<TextView>(R.id.announce).setText(player.name+"に"+max(damage, 1)+"ダメージ")
                    if(damage >= player.hp/3) {
                        soundPool.play(se_monster_critical, 1.0f, 1.0f, 1, 0, 1.0f)
                    }else{
                        soundPool.play(se_monster_attack, 1.0f, 1.0f, 1, 0, 1.0f)
                    }
                }, latency.toLong())
            }
        }else{
            latency = latency + 1000
            Handler().postDelayed(Runnable {
                soundPool.play(se_miss, 1.0f, 1.0f, 1, 0, 1.0f)
                findViewById<TextView>(R.id.announce).setText("しかし"+monster!!.name+"の攻撃は外れた")
            }, latency.toLong())
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
        findViewById<TextView>(R.id.monster_condition).setText("")
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
