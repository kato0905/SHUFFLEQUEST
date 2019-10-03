package com.kato0905.shufflequest

import io.realm.RealmObject

open class ItemModel(
        open var id : Int = 0,
        open var name : String = "",
        open var cost : Int = 0,
        open var power : Int = 0,
        open var explain : String = "",
        open var current : Int = 0,
        open var max : Int = 0,
        open var set : Int = 0
) : RealmObject() {}