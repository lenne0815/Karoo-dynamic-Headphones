package com.lenne0815.karooheadphones.extension

import android.content.Context
import io.hammerhead.karoo.ext.KarooExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DynamicHeadphonesExtension(context: Context) : KarooExtension("karoo-dynamic-headphones", "1.0") {
    
    private val extensionScope = CoroutineScope(Dispatchers.Default + Job())
    
    private var speedListener: ((Double) -> Unit)? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Simulated speed data stream for initial build
        // TODO: Replace with actual Karoo SDK streaming when API is confirmed
        extensionScope.launch {
            while (isActive) {
                // Placeholder: emit test speed
                SpeedDataEmitter.emit(25.0)
                speedListener?.invoke(25.0)
                delay(1000)
            }
        }
    }
    
    fun setSpeedListener(listener: (Double) -> Unit) {
        speedListener = listener
    }
    
    companion object {
        @Volatile
        private var instance: DynamicHeadphonesExtension? = null
        
        fun getInstance(context: Context): DynamicHeadphonesExtension {
            return instance ?: synchronized(this) {
                instance ?: DynamicHeadphonesExtension(context).also { 
                    instance = it 
                }
            }
        }
    }
}
