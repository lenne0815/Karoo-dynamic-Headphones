package com.lenne0815.karooheadphones

import android.app.Application
import com.lenne0815.karooheadphones.extension.SpeedDataEmitter
import com.lenne0815.karooheadphones.service.DynamicHeadphonesService

class KarooHeadphonesApp : Application() {
    
    private var headphonesService: DynamicHeadphonesService? = null
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Connect Extension to Service via emitter
        SpeedDataEmitter.setListener { speed ->
            headphonesService?.updateSpeed(speed)
        }
    }
    
    fun setHeadphonesService(service: DynamicHeadphonesService?) {
        headphonesService = service
    }
    
    companion object {
        lateinit var instance: KarooHeadphonesApp
            private set
    }
}