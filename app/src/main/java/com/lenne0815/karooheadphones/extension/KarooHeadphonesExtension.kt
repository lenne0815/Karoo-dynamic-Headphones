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

/**
 * Karoo Extension that provides speed data to the Dynamic Headphones Service
 */
class KarooHeadphonesExtension(context: Context) : KarooExtension("karoo-dynamic-headphones", BuildConfig.VERSION_NAME) {
    
    private val extensionScope = CoroutineScope(Dispatchers.Default + Job())
    
    init {
        // Listen to speed data from Karoo
        extensionScope.launch {
            streamData(DataType.Speed(HardwareType.BIKE)).collectLatest { state ->
                when (state) {
                    is StreamState.Streaming -> {
                        val speedMps = state.dataPoint.singleValue ?: 0.0
                        val speedKmh = speedMps * 3.6
                        // Notify the service about speed update
                        SpeedDataEmitter.updateSpeed(speedKmh)
                    }
                    else -> {
                        // Speed sensor not available or searching
                    }
                }
            }
        }
    }
    
    companion object {
        private var serviceInstance: com.lenne0815.karooheadphones.service.DynamicHeadphonesService? = null
        
        fun setService(service: com.lenne0815.karooheadphones.service.DynamicHeadphonesService?) {
            serviceInstance = service
        }
    }
}

/**
 * Simple emitter to pass speed data from Extension to Service
 */
object SpeedDataEmitter {
    private var listener: ((Double) -> Unit)? = null
    
    fun setListener(callback: (Double) -> Unit) {
        listener = callback
    }
    
    fun updateSpeed(speedKmh: Double) {
        listener?.invoke(speedKmh)
    }
}