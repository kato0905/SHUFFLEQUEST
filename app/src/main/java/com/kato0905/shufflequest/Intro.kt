package com.kato0905.shufflequest

import android.content.Intent
//import android.support.v7.app.AppCompatActivity
import android.app.Activity
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import org.w3c.dom.Text

class Intro : Activity() {
    private lateinit var soundPool: SoundPool
    private lateinit var intro_bgm: MediaPlayer

    var intro_story = arrayOf(
            "やっと目が覚めたか",
            "お前が眠っている間に魔王軍が人類に対し進行を開始した",
            "人類は国家間の大戦にかまけている。国が軍を出す頃には手遅れになっているだろう",
            "早急に魔王を倒す必要がある",
            "そのためにお前に魔法を授ける",
            "シャッフルという魔法だ",
            "これを使用すると全て生き物のステータスがランダムにシャッフルされる",
            "この魔法を使うことで、攻撃力が9999のスライムやHPが20の魔王を作ることができる",
            "自分のステータスもシャッフルによって変わるから注意するように",
            "この魔法を活用して魔王を倒してくれ",
            "シャッフルはステータス画面から使える",
            "頼んだぞ"
    )

    var result_story = arrayOf(
            "死んでしまったのか",
            "魔王を倒すため、まだお前には頑張ってもらわないといけない",
            "さあ、旅立つのだ"
    )

    var clear_story = arrayOf(
            "…………………………………………",
            "魔王討伐ご苦労だった",
            "これで私の願いも叶う",
            "シャッフルを使っていて不便と思わなかったか？　自分の良いステータスがシャッフルされ歯痒かっただろう",
            "だから私はなりたいのだ",
            "魔王の魂を触媒に、決してシャッフルされない最強の生物に",
            "そのためにお前を利用させてもらった",
            "自分のステータスもシャッフルされては魔王を倒すどころではないからな",
            "お前は十分役に立ってくれた",
            "だがもう用済みだ"
    )

    var grudge_death_story = arrayOf(
            "ゲームオーバーだ",
            "悔しかったら、また倒しに来い",
            "これから私は",
            "世界を滅ぼす"
    )

    var true_clear_story = arrayOf(
            "………………………………………",
            "私が…負けるとは……",
            "全てのモンスターより速く、全てのモンスターよりも堅かった私が…",
            "無念……だ…"
    )

    var i = 0

    var content = "content"

    lateinit var realm: Realm

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

        intro_bgm = MediaPlayer.create(this, R.raw.intro_bgm)
        intro_bgm.setVolume(0.5f, 0.5f)
        intro_bgm.setLooping(true)
        intro_bgm.start()

    }

    override fun onStop(){
        super.onStop()
        soundPool?.release()
        intro_bgm.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

        content = intent.getStringExtra("content")
        when(content) {
            "intro" -> findViewById<TextView>(R.id.intro_text).setText(intro_story[i])
            "result" -> {
                findViewById<TextView>(R.id.intro_text).setText(result_story[i])
                // Realmのセットアップ
                val realmConfig = RealmConfiguration.Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build()
                realm = Realm.getInstance(realmConfig)
                realm.beginTransaction()
            }
            "clear" -> findViewById<TextView>(R.id.intro_text).setText(clear_story[i])
            "grudge_death" -> findViewById<TextView>(R.id.intro_text).setText(grudge_death_story[i])
            "true_clear" -> {
                findViewById<ImageView>(R.id.intro_image).setImageResource(R.drawable.grudge)
                findViewById<TextView>(R.id.intro_text).setText(true_clear_story[i])
            }
            else -> Log.d("system_output", "Error in intro_content")
        }
    }

    override fun onTouchEvent(event: MotionEvent) :Boolean {
        val cord : TextView = findViewById(R.id.intro_text)

        when(event.getAction()) {
            MotionEvent.ACTION_UP -> {
                i++
                when(content) {
                    "intro" -> {
                        if(i < intro_story.size){
                            cord.text = intro_story[i]
                        }else{
                            val intent = Intent(this, Map_town::class.java)
                            soundPool?.release()
                            intro_bgm.release()
                            finish()
                            startActivity(intent)
                        }
                    }
                    "result" -> {
                        if(i < result_story.size){
                            cord.text = result_story[i]
                        }else{


                            val load_magic = realm.where(MagicModel::class.java).equalTo("canuse",0.toInt()).findAll()
                            val player = realm.where(PlayerModel::class.java).findFirst()
                            run loop@{
                                load_magic.forEach() {
                                    if ((0..100).random() <= 30) {
                                        it.canuse = 1
                                        Toast.makeText(applicationContext, "魔法が一つアンロックされました", Toast.LENGTH_LONG).show()
                                        return@loop
                                    }
                                }
                            }
                            player!!.current_hp = player.hp
                            player.current_mp = player.mp
                            realm.commitTransaction()
                            realm.close()

                            val intent = Intent(this, Map_town::class.java)
                            soundPool?.release()
                            intro_bgm.release()
                            finish()
                            startActivity(intent)
                        }
                    }
                    "clear" -> {
                        if(i < clear_story.size){
                            cord.text = clear_story[i]
                            if(i == clear_story.size - 1){
                                findViewById<ImageView>(R.id.intro_image).setImageResource(R.drawable.grudge)
                            }
                        }else{
                            val intent = Intent(this, Battle::class.java)
                            intent.putExtra("current_progress","101")
                            soundPool?.release()
                            intro_bgm.release()
                            finish()
                            startActivity(intent)
                        }
                    }
                    "grudge_death" -> {
                        if(i < grudge_death_story.size){
                            cord.text = grudge_death_story[i]
                        }else{
                            val intent = Intent(this, Title::class.java)
                            soundPool?.release()
                            intro_bgm.release()
                            finish()
                            startActivity(intent)
                        }
                    }
                    "true_clear" -> {
                        if(i < true_clear_story.size){
                            cord.text = true_clear_story[i]
                            findViewById<ImageView>(R.id.intro_image).setImageAlpha(255-255*i/true_clear_story.size)
                        }else{
                            val intent = Intent(this, Game_clear::class.java)
                            soundPool?.release()
                            intro_bgm.release()
                            finish()
                            startActivity(intent)
                        }
                    }
                    else -> Log.d("system_output","Error in intro_onTouchEvent")
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
