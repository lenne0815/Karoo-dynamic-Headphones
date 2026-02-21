package com.lenne0815.karooheadphones.extension

import android.content.Context
import io.hammerhead.karoo.ext.KarooExtension
import io.hammerhead.karoo.ext.models.DataType
import io.hammerhead.karoo.ext.models.HardwareType
import io.hammerhead.karoo.ext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DynamicHeadphonesExtension(context: Context) : KarooExtension("karoo-dynamic-headphones", "1.0") {
    
    private val extensionScope = CoroutineScope(Dispatchers.Default + Job())
    
    private var speedListener: ((Double) -> Unit)? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Listen to speed data from Karoo
        extensionScope.launch {
            streamData(DataType.Speed(HardwareType.BIKE)).collectLatest { state ->
                when (state) {
                    is StreamState.Streaming -> {
                        val speedMps = state.dataPoint.singleValue ?: 0.0
                        val speedKmh = speedMps * 3.6
                        SpeedDataEmitter.emit(speedKmh)
                        speedListener?.invoke(speedKmh)
                    }
                    else -> {
                        // Speed sensor not available or idle
                    }
                }
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