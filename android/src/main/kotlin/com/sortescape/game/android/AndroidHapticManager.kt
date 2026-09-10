package com.sortescape.game.android

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.sortescape.game.audio.HapticManager

class AndroidHapticManager(context: Context) : HapticManager {

    private var enabled = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun vibrate(ms: Long, amplitude: Int) {
        if (!enabled) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, amplitude.coerceIn(1, 255)))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(ms)
        }
    }

    override fun tap() = vibrate(15, 60)
    override fun correctSort() = vibrate(30, 120)
    override fun combo() = vibrate(45, 200)
    override fun levelComplete() = vibrate(150, 255)

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
