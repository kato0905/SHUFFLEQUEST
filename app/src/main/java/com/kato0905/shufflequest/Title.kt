package com.kato0905.shufflequest

//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_title.*
import java.io.*

class Title : Activity() {

    lateinit var mRealm: Realm


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        Realm.init(this)


        // Realmのセットアップ
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)


        //初めから
        start_button.setOnClickListener{
            insertMagic()
            insertEnemy()
            insertPlayer()
            insertItem()
            mRealm.close()

            val intent = Intent(this, Intro::class.java)
            intent.putExtra("content", "intro")
            finish()
            startActivity(intent)
        }

        //続きから
        continue_button.setOnClickListener{
            mRealm.close()
            val intent = Intent(this, Map_town::class.java)
            finish()
            startActivity(intent)
        }
    }

    fun insertMagic(){
        mRealm.executeTransaction {
            //中身をリセット
            mRealm.where(MagicModel::class.java).findAll().deleteAllFromRealm()

            //canuse
            //0 -> まだ使えない
            //1 -> 使えるがセットされていない
            //2 -> セットされていつでも使用できる
            //常に2が3個になるようにする
            mRealm.insert(MagicModel(1,"ファイヤ", 30, 40, 2))
            mRealm.insert(MagicModel(2,"サンダー", 30, 40, 2))
            mRealm.insert(MagicModel(3,"アイス", 30, 40, 2))
            mRealm.insert(MagicModel(4,"スリープ", 20, 20, 0))
            mRealm.insert(MagicModel(5,"ワープ", 10, 0, 0))
            mRealm.insert(MagicModel(6,"レイズ", 20, 30, 0))
            mRealm.insert(MagicModel(7,"ハード", 20, 30, 0))
            mRealm.insert(MagicModel(8,"ラピッド", 20, 30, 0))
            mRealm.insert(MagicModel(9,"ポイズン", 30, 10, 0))
            mRealm.insert(MagicModel(10,"ヒール", 40, 30, 1))
            mRealm.insert(MagicModel(11,"ブレード", 40, 200, 0))
            mRealm.insert(MagicModel(12,"ライトニング", 40, 150, 0))
            mRealm.insert(MagicModel(13,"バッシュ", 40, 150, 0))
        }
    }

    fun insertEnemy(){
        mRealm.executeTransaction{
            //中身をリセット
            mRealm.where(EnemyModel::class.java).findAll().deleteAllFromRealm()

            mRealm.insert(EnemyModel(1, "スライム", 120, 30, 30, 20, 20, 30, 100, 100, 100, 100, 100, 1, 20, "slime"))

            mRealm.insert(EnemyModel(2, "ゴブリン", 160, 30, 50, 40, 30, 20, 100, 100, 100, 100, 100, 1, 30, "goblin"))

            mRealm.insert(EnemyModel(3, "大蛇", 180, 50, 60, 50, 60, 50, 100, 100, 100, 100, 100, 1, 60, "snake"))

            mRealm.insert(EnemyModel(4, "オーク", 200, 20, 80, 60, 120, 30, 100, 100, 100, 100, 100, 1, 100, "awk"))

            mRealm.insert(EnemyModel(5, "ゾンビ", 300, 30, 100, 60, 80, 40, 100, 100, 100, 90, 100, 1, 120, "zonbie"))

            mRealm.insert(EnemyModel(6, "人喰虫", 300, 60, 120, 80, 140, 30, 100, 100, 90, 100, 100, 1, 160, "maneater"))

            mRealm.insert(EnemyModel(7, "トレント", 400, 120, 160, 100, 100, 60, 100, 150, 100, 100, 100, 1, 240, "torrent"))

            mRealm.insert(EnemyModel(8, "リザードマン", 600, 100, 200, 160, 160, 80, 90, 100, 90, 100, 100, 1, 260, "lizard"))

            mRealm.insert(EnemyModel(9, "スケルトン", 800, 400, 300, 200, 200, 60, 95, 100, 100, 95, 100, 1, 300, "skeleton"))

            mRealm.insert(EnemyModel(10, "ゴーレム", 3000, 10, 1000, 600, 10, 10, 10, 100, 100, 100, 100, 1, 350, "golem"))

            mRealm.insert(EnemyModel(11, "魔王兵", 1800, 800, 400, 400, 600, 80, 75, 80, 100, 100, 100, 1, 400, "soldier"))

            mRealm.insert(EnemyModel(12, "ダイナソー", 2400, 500, 1600, 800, 900, 120, 80, 100, 80, 90, 100, 1, 430, "dinosaur"))

            mRealm.insert(EnemyModel(13, "ネクロマンサー", 1600, 1600, 500, 600, 700, 80, 80, 100, 100, 100, 100, 1, 350, "necro"))

            mRealm.insert(EnemyModel(14, "デビル", 2000, 2000, 600, 800, 800, 80, 80, 80, 100, 80, 100, 1, 520, "devil"))

            mRealm.insert(EnemyModel(15, "死霊", 2500, 2000, 800, 600, 1000, 100, 0, 80, 80, 100, 100, 1, 540, "deadman"))

            mRealm.insert(EnemyModel(16, "魔王", 9999, 9999, 1600, 1200, 1600, 60, 30, 30, 30, 30, 10, 1, 1600, "mao"))

            mRealm.insert(EnemyModel(17, "怨念", 9999, 9999, 1600, 1200, 1600, 60, 20, 20, 20, 20, 20, 1, 2000, "grudge"))

        }
    }

    fun insertPlayer(){
        mRealm.executeTransaction{
            //中身をリセット
            mRealm.where(PlayerModel::class.java).findAll().deleteAllFromRealm()

            mRealm.insert(PlayerModel(1, "勇者", 150, 100, 60, 40, 50, 40, 40, 0, 0, 0, 150, 100))
        }
    }

    fun insertItem(){
        mRealm.executeTransaction{
            //中身をリセット
            mRealm.where(ItemModel::class.java).findAll().deleteAllFromRealm()

            mRealm.insert(ItemModel(1, "薬草", 150, 20, "HPを固定値回復する", 1,5,1))
            mRealm.insert(ItemModel(2, "聖水", 150, 20, "MPを固定値回復する", 1,5,0))
            mRealm.insert(ItemModel(3, "上薬草", 300, 20, "HPを割合回復する", 0,3,0))
            mRealm.insert(ItemModel(4, "上聖水", 300, 20, "MPを割合回復する", 0,3,0))
            mRealm.insert(ItemModel(5, "人形", 1500, 1, "死亡時HPを1で復活", 0,1,0))
            mRealm.insert(ItemModel(6, "力の指輪", 1800, 30, "攻撃力を固定値増加", 0,1,0))
            mRealm.insert(ItemModel(7, "防の指輪", 1800, 30, "防御力を固定値増加", 0,1,0))
            mRealm.insert(ItemModel(8, "速の指輪", 1800, 30, "素早さを固定値増加", 0,1,0))
            mRealm.insert(ItemModel(9, "魔耐の指輪", 1800, 30, "魔耐を固定値増加", 0,1,0))
        }
    }


}
