package com.lenne0815.karooheadphones.extension

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Simplified extension service without Karoo SDK dependency.
 * This is a stub implementation that simulates speed data for testing.
 * 
 * TODO: Restore KarooExtension when SDK is available
 */
class DynamicHeadphonesExtension : Service() {
    
    private val extensionScope = CoroutineScope(Dispatchers.Default + Job())
    
    private var speedListener: ((Double) -> Unit)? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Simulated speed data stream for testing
        extensionScope.launch {
            while (isActive) {
                // Placeholder: emit test speed (25 km/h)
                SpeedDataEmitter.emit(25.0)
                speedListener?.invoke(25.0)
                delay(1000)
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    fun setSpeedListener(listener: (Double) -> Unit) {
        speedListener = listener
    }
    
    companion object {
        @Volatile
        private var instance: DynamicHeadphonesExtension? = null
        
        fun getInstance(context: Context): DynamicHeadphonesExtension {
            return instance ?: synchronized(this) {
                instance ?: DynamicHeadphonesExtension().also { 
                    instance = it 
                }
            }
        }
    }
}
