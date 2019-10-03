package com.kato0905.shufflequest

import android.content.Intent
//import android.support.v7.app.AppCompatActivity
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import org.w3c.dom.Text

class Intro : Activity() {

    var intro_story1 = arrayOf(
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
            "頼んだぞ"
    )

    var intro_story = arrayOf(
            "s",
            "b"
    )

    var result_story = arrayOf(
            "死んでしまったのか",
            "魔王を倒すため、まだお前には頑張ってもらわないといけない",
            "さあ、旅立つのだ"
    )

    var clear_story = arrayOf(
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

    var i = 0

    var content = "content"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        content = intent.getStringExtra("content")
        when(content) {
            "intro" -> findViewById<TextView>(R.id.intro_text).setText(intro_story[i])
            "result" -> findViewById<TextView>(R.id.intro_text).setText(result_story[i])
            "clear" -> findViewById<TextView>(R.id.intro_text).setText(clear_story[i])
            "grudge_death" -> findViewById<TextView>(R.id.intro_text).setText(grudge_death_story[i])
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
                            finish()
                            startActivity(intent)
                        }
                    }
                    "result" -> {
                        if(i < result_story.size){
                            cord.text = result_story[i]
                        }else{
                            val intent = Intent(this, Map_town::class.java)
                            finish()
                            startActivity(intent)
                        }
                    }
                    "clear" -> {
                        if(i < clear_story.size){
                            cord.text = clear_story[i]
                        }else{
                            val intent = Intent(this, Battle::class.java)
                            intent.putExtra("current_progress","101")
                            finish()
                            startActivity(intent)
                        }
                    }
                    "grudge_death" -> {
                        if(i < grudge_death_story.size){
                            cord.text = grudge_death_story[i]
                        }else{
                            val intent = Intent(this, Title::class.java)
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