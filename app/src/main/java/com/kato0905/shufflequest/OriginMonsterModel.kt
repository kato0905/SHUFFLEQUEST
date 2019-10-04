package com.kato0905.shufflequest

import io.realm.RealmObject

open class OriginMonsterModel(
        open var id : Int = 0,
        open var name : String = "",
        open var hp : Int = 0,
        open var mp : Int = 0,
        open var attack : Int = 0,
        open var defense : Int = 0,
        open var speed : Int = 0,
        open var dex : Int = 0,
        open var resist : Int = 0,
        open var fire : Int = 0,
        open var thunder : Int = 0,
        open var ice : Int = 0,
        open var poison : Int = 0,
        open var chara : Int = 0,
        open var drop : Int = 0,
        open var imagename : String = ""
) : RealmObject() {}