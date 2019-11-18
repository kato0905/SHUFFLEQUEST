package com.kato0905.shufflequest2

import io.realm.RealmObject

open class PushPlayerModel(
        open var id : Int = 0,
        open var hp : String = "",
        open var mp : String = "",
        open var attack : String = "",
        open var defense : String = "",
        open var speed : String = "",
        open var dex : String = "",
        open var mdef : String = ""
) : RealmObject() {}