package com.lenne0815.karooheadphones

import android.app.Application

class KarooHeadphonesApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: KarooHeadphonesApp
            private set
    }
}