package com.kato0905.shufflequest

import io.realm.RealmObject

open class PushMonsterModel(
        open var id : Int = 0,
        open var hp : String = "",
        open var mp : String = "",
        open var attack : String = "",
        open var defense : String = "",
        open var speed : String = "",
        open var dex : String = ""
) : RealmObject() {}