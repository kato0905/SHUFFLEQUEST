package com.kato0905.shufflequest

import io.realm.RealmObject

open class OriginPlayerModel(
        open var id : Int = 0,
        open var name : String = "",
        open var hp : Int = 0,
        open var mp : Int = 0,
        open var attack : Int = 0,
        open var defense : Int = 0,
        open var speed : Int = 0,
        open var dex : Int = 0,
        open var mdef : Int = 0,
        open var weapon : Int = 0,
        open var armor : Int = 0,
        open var money : Int = 0,
        open var current_hp : Int = 0,
        open var current_mp : Int = 0
) : RealmObject() {}