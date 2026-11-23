package com.example.resepedia.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pusat Pengaturan Animasi (Motion) Aplikasi Resepedia:
 * Mengatur durasi, kurva easing, dan stagger delay terpusat agar seluruh animasi di aplikasi konsisten.
 */
object Motion {
    // Durasi Animasi Terstandar
    const val DURATION_SHORT: Int = 150
    const val DURATION_MEDIUM: Int = 300
    const val DURATION_LONG: Int = 500

    // Kurva Easing Standar Material Design
    val EasingStandard: Easing = FastOutSlowInEasing
    val EasingEmphasized: Easing = LinearOutSlowInEasing

    // Pengaturan Animasi Bertahap (Staggered Entrance)
    const val STAGGER_DELAY_MS: Int = 40
    const val STAGGER_ITEM_LIMIT: Int = 8

    /**
     * Helper Tween Spec Singkat (150 ms)
     */
    fun <T> shortTween(delayMillis: Int = 0) = tween<T>(
        durationMillis = DURATION_SHORT,
        delayMillis = delayMillis,
        easing = EasingStandard
    )

    /**
     * Helper Tween Spec Menengah (300 ms)
     */
    fun <T> mediumTween(delayMillis: Int = 0) = tween<T>(
        durationMillis = DURATION_MEDIUM,
        delayMillis = delayMillis,
        easing = EasingStandard
    )

    /**
     * Helper Tween Spec Panjang (500 ms)
     */
    fun <T> longTween(delayMillis: Int = 0) = tween<T>(
        durationMillis = DURATION_LONG,
        delayMillis = delayMillis,
        easing = EasingStandard
    )
}

/**
 * Modifier helper untuk animasi masuk bertahap (fade + translasi Y kecil).
 * Hanya diterapkan pada batas item awal dan hanya berjalan satu kali untuk menjaga performa rendering saat scroll.
 */
@Composable
fun Modifier.staggeredEntrance(
    index: Int,
    enabled: Boolean = true
): Modifier {
    if (!enabled || index >= Motion.STAGGER_ITEM_LIMIT) {
        return this
    }

    val alphaAnim = remember { Animatable(0f) }
    val translateYAnim = remember { Animatable(24f) }

    LaunchedEffect(index) {
        val delayTime = (index * Motion.STAGGER_DELAY_MS).toLong()
        delay(delayTime)
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = Motion.mediumTween()
            )
        }
        launch {
            translateYAnim.animateTo(
                targetValue = 0f,
                animationSpec = Motion.mediumTween()
            )
        }
    }

    return this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = translateYAnim.value
    }
}
