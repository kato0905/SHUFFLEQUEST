package com.kato0905.shufflequest

import io.realm.RealmObject

open class MagicModel(
        open var id : Int = 0,
        open var name : String = "",
        open var mp : Int = 0,
        open var power : Int = 0,
        open var canuse : Int = 0,
        open var explain : String = "",
        open var cost : Int = 0
) : RealmObject() {}