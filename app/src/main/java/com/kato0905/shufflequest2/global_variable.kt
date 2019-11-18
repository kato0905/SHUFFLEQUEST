package com.kato0905.shufflequest2

import android.app.Application

class MyApp : Application() {

    var monster_num = 16
    var magic_num = 3
    var item_num = 1
    var player_num = 1


    override fun onCreate() {
        super.onCreate()
    }
}