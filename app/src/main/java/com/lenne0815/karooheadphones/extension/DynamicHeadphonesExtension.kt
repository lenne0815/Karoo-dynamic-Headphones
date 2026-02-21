package com.lenne0815.karooheadphones.extension

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Simplified extension class without Karoo SDK dependency.
 * This is a stub implementation that simulates speed data for testing.
 * 
 * TODO: Restore KarooExtension when SDK is available
 */
class DynamicHeadphonesExtension(context: Context) {
    
    private val extensionScope = CoroutineScope(Dispatchers.Default + Job())
    
    private var speedListener: ((Double) -> Unit)? = null
    
    fun onCreate() {
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
