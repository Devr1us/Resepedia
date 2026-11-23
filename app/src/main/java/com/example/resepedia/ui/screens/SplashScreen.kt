package com.example.resepedia.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resepedia.R
import com.example.resepedia.ui.components.AppLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Layar animasi pembuka (Entrance / Splash Screen)
 * Urutan animasi (total ~1.8 detik):
 * 1. Logo muncul: scale 0.6 -> 1.0 (spring / overshoot halus) + fade-in.
 * 2. Denyut halus logo (pulse 1.0 -> 1.08 -> 1.0).
 * 3. Teks Resepedia & Tagline muncul: fade-in + slide-up dari bawah.
 * 4. Fade-out seluruh konten dan transisi ke Beranda.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(30f) }
    val contentAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 1. Logo muncul dengan scale 0.6 -> 1.0 (overshoot spring halus) + fade-in (~600ms)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // 2. Denyut halus (pulse) logo 1.0 -> 1.08 -> 1.0 (~350ms)
        logoScale.animateTo(
            targetValue = 1.08f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing)
        )

        // 3. Teks judul dan tagline muncul: fade-in + slide dari bawah (~400ms)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        textOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // Tahan sejenak agar logo & teks terbaca jelas (~350ms)
        delay(350)

        // 4. Seluruh konten fade-out halus (~350ms)
        contentAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )

        // Navigasi ke Dashboard
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer { alpha = contentAlpha.value },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Resepedia Utama
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    alpha = logoAlpha.value
                }
            ) {
                AppLogo(size = 96.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Teks Nama Aplikasi & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffsetY.value
                }
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
