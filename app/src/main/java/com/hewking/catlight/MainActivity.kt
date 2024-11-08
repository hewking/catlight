package com.hewking.catlight

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var lightScreen: View
    private lateinit var toggleButton: MaterialButton
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var colorTempSeekBar: SeekBar
    private var isLightOn = false
    private var originalBrightness = 0
    private var currentColorTemp = 6500 // 默认色温 6500K

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 保存原始屏幕亮度
        originalBrightness = Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )

        initViews()
        setupListeners()
    }

    private fun initViews() {
        lightScreen = findViewById(R.id.lightScreen)
        toggleButton = findViewById(R.id.toggleButton)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)
        colorTempSeekBar = findViewById(R.id.colorTempSeekBar)

        // 初始化亮度滑块
        brightnessSeekBar.progress = 50
        // 初始化色温滑块（设为中间值）
        colorTempSeekBar.progress = 50
    }

    private fun setupListeners() {
        toggleButton.setOnClickListener {
            isLightOn = !isLightOn
            if (isLightOn) {
                turnOnLight()
                toggleButton.text = "关闭补光灯"
            } else {
                turnOffLight()
                toggleButton.text = "开启补光灯"
            }
        }

        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isLightOn) {
                    updateScreenBrightness(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        colorTempSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isLightOn) {
                    updateColorTemperature(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun turnOnLight() {
        // 设置屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 设置全屏亮度
        updateScreenBrightness(brightnessSeekBar.progress)
        // 设置色温
        updateColorTemperature(colorTempSeekBar.progress)
        lightScreen.visibility = View.VISIBLE
    }

    private fun turnOffLight() {
        // 取消屏幕常亮
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 恢复原始亮度
        val layoutParams = window.attributes
        layoutParams.screenBrightness = originalBrightness / 255f
        window.attributes = layoutParams
        lightScreen.visibility = View.GONE
    }

    private fun updateScreenBrightness(progress: Int) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = progress / 100f
        window.attributes = layoutParams
    }

    private fun updateColorTemperature(progress: Int) {
        // 将进度转换为色温值（3000K-9000K）
        currentColorTemp = 3000 + (progress * 60)
        
        // 将色温转换为RGB颜色
        val color = colorTemperatureToRGB(currentColorTemp)
        lightScreen.setBackgroundColor(color)
    }

    private fun colorTemperatureToRGB(temperature: Int): Int {
        var temp = temperature / 100.0
        var red: Double
        var green: Double
        var blue: Double

        if (temp <= 66) {
            red = 255.0
            green = temp.coerceIn(0.0, 99.0)
            green = 99.4708025861 * Math.log(green) - 161.1195681661
            if (temp <= 19) {
                blue = 0.0
            } else {
                blue = temp - 10
                blue = 138.5177312231 * Math.log(blue) - 305.0447927307
            }
        } else {
            temp -= 60
            red = 329.698727446 * Math.pow(temp, -0.1332047592)
            green = 288.1221695283 * Math.pow(temp, -0.0755148492)
            blue = 255.0
        }

        return Color.rgb(
            red.coerceIn(0.0, 255.0).toInt(),
            green.coerceIn(0.0, 255.0).toInt(),
            blue.coerceIn(0.0, 255.0).toInt()
        )
    }

    override fun onStop() {
        super.onStop()
        if (isLightOn) {
            turnOffLight()
        }
    }
} 