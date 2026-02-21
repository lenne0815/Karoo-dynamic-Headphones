package com.lenne0815.karooheadphones.extension

object SpeedDataEmitter {
    private var listener: ((Double) -> Unit)? = null
    
    fun setListener(newListener: (Double) -> Unit) {
        listener = newListener
    }
    
    fun emit(speed: Double) {
        listener?.invoke(speed)
    }
}
