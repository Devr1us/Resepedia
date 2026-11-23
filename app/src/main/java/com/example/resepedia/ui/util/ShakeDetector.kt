package com.example.resepedia.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.sqrt

/**
 * Utilitas untuk mendeteksi ketersediaan sensor akselerometer pada perangkat.
 */
object ShakeDetector {
    /**
     * Memeriksa apakah sensor accelerometer tersedia pada perangkat fisik.
     */
    fun hasAccelerometer(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    }
}

/**
 * Composable lifecycle-aware yang mendeteksi goyangan/guncangan (shake) perangkat.
 *
 * Menggunakan SensorManager dengan tipe TYPE_ACCELEROMETER.
 * - Ambang batas: ~2.5g (sesuai spesifikasi guncangan wajar).
 * - Cooldown / Debounce: 1.5 detik untuk menghindari pemicuan ganda beruntun.
 * - Hemat daya: Sensor HANYA didaftarkan saat siklus hidup layar berada pada status ON_RESUME,
 *   dan dilepaskan (unregister) saat ON_PAUSE atau layar ditutup.
 */
@Composable
fun ShakeEffect(
    enabled: Boolean = true,
    thresholdG: Float = 2.5f,
    cooldownMs: Long = 1500L,
    onShake: () -> Unit
) {
    if (!enabled) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(context, lifecycleOwner, enabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose {}
        }

        var lastShakeTime = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

                val x = event.values[0] / SensorManager.GRAVITY_EARTH
                val y = event.values[1] / SensorManager.GRAVITY_EARTH
                val z = event.values[2] / SensorManager.GRAVITY_EARTH

                val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                if (gForce >= thresholdG) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime >= cooldownMs) {
                        lastShakeTime = now
                        onShake()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Tidak diperlukan penanganan perubahan akurasi
            }
        }

        var isRegistered = false

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!isRegistered) {
                        sensorManager.registerListener(
                            listener,
                            accelerometer,
                            SensorManager.SENSOR_DELAY_UI
                        )
                        isRegistered = true
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (isRegistered) {
                        sensorManager.unregisterListener(listener)
                        isRegistered = false
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // Daftarkan listener jika saat ini layar sudah berada dalam keadaan RESUMED
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            isRegistered = true
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (isRegistered) {
                sensorManager.unregisterListener(listener)
            }
        }
    }
}
