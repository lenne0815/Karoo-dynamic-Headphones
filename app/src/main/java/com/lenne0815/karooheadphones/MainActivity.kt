package com.lenne0815.karooheadphones

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.lenne0815.karooheadphones.service.DynamicHeadphonesService

class MainActivity : AppCompatActivity() {
    
    private var headphonesService: DynamicHeadphonesService? = null
    private var serviceBound = false
    
    private lateinit var switchEnable: Switch
    private lateinit var switchMode: Switch
    private lateinit var cardDynamicSettings: CardView
    private lateinit var cardNormalSettings: CardView
    private lateinit var seekBarPauseThreshold: SeekBar
    private lateinit var textPauseThreshold: TextView
    private lateinit var seekBarMinVolume: SeekBar
    private lateinit var seekBarMaxVolume: SeekBar
    private lateinit var seekBarDefaultVolume: SeekBar
    private lateinit var textDefaultVolume: TextView
    private lateinit var textSpeed: TextView
    private lateinit var textStatus: TextView
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DynamicHeadphonesService.LocalBinder
            headphonesService = binder.getService()
            serviceBound = true
            
            headphonesService?.onSpeedUpdate = { speed ->
                runOnUiThread {
                    textSpeed.text = "${String.format("%.1f", speed)} km/h"
                }
            }
            headphonesService?.onStatusUpdate = {
                runOnUiThread {
                    updateStatus()
                }
            }
            
            updateUI()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            headphonesService = null
            serviceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Intent(this, DynamicHeadphonesService::class.java).also { intent ->
            startService(intent)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        
        initializeViews()
        setupListeners()
    }
    
    private fun initializeViews() {
        switchEnable = findViewById(R.id.switch_enable)
        switchMode = findViewById(R.id.switch_mode)
        cardDynamicSettings = findViewById(R.id.card_dynamic_settings)
        cardNormalSettings = findViewById(R.id.card_normal_settings)
        seekBarPauseThreshold = findViewById(R.id.seekbar_pause_threshold)
        textPauseThreshold = findViewById(R.id.text_pause_threshold)
        seekBarMinVolume = findViewById(R.id.seekbar_min_volume)
        seekBarMaxVolume = findViewById(R.id.seekbar_max_volume)
        seekBarDefaultVolume = findViewById(R.id.seekbar_default_volume)
        textDefaultVolume = findViewById(R.id.text_default_volume)
        textSpeed = findViewById(R.id.text_speed)
        textStatus = findViewById(R.id.text_status)
    }
    
    private fun setupListeners() {
        switchEnable.setOnCheckedChangeListener { _, isChecked ->
            headphonesService?.setEnabled(isChecked)
            updateStatus()
        }
        
        switchMode.setOnCheckedChangeListener { _, isChecked ->
            headphonesService?.setDynamicMode(isChecked)
            updateModeUI(isChecked)
            updateStatus()
        }
        
        seekBarPauseThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val threshold = 0.1 + (progress * 0.1)
                textPauseThreshold.text = "${String.format("%.1f", threshold)} km/h"
                if (fromUser) {
                    headphonesService?.setPauseThreshold(threshold)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        seekBarMinVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    headphonesService?.setVolumeRange(progress, seekBarMaxVolume.progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        seekBarMaxVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    headphonesService?.setVolumeRange(seekBarMinVolume.progress, progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        seekBarDefaultVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textDefaultVolume.text = "$progress%"
                if (fromUser) {
                    headphonesService?.setDefaultVolume(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun updateModeUI(isDynamic: Boolean) {
        if (isDynamic) {
            cardDynamicSettings.visibility = CardView.VISIBLE
            cardNormalSettings.visibility = CardView.GONE
        } else {
            cardDynamicSettings.visibility = CardView.GONE
            cardNormalSettings.visibility = CardView.VISIBLE
        }
    }
    
    private fun updateUI() {
        headphonesService?.let { service ->
            switchEnable.isChecked = service.isServiceEnabled()
            switchMode.isChecked = service.isDynamicModeEnabled()
            
            val threshold = service.getPauseThreshold()
            seekBarPauseThreshold.progress = ((threshold - 0.1) / 0.1).toInt()
            textPauseThreshold.text = "${String.format("%.1f", threshold)} km/h"
            
            seekBarMinVolume.progress = service.getMinVolume()
            seekBarMaxVolume.progress = service.getMaxVolume()
            seekBarDefaultVolume.progress = service.getDefaultVolume()
            textDefaultVolume.text = "${service.getDefaultVolume()}%"
            
            textSpeed.text = "${String.format("%.1f", service.getCurrentSpeed())} km/h"
            
            updateModeUI(service.isDynamicModeEnabled())
        }
        updateStatus()
    }
    
    private fun updateStatus() {
        val status = if (headphonesService?.isServiceEnabled() == true) {
            val mode = if (headphonesService?.isDynamicModeEnabled() == true) {
                "Dynamic Mode"
            } else {
                "Normal Mode"
            }
            "Active - $mode"
        } else {
            "Disabled"
        }
        textStatus.text = status
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}