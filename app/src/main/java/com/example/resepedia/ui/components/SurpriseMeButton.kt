package com.example.resepedia.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resepedia.R
import com.example.resepedia.ui.theme.Motion
import com.example.resepedia.ui.theme.PrimaryOrange
import kotlinx.coroutines.delay

/**
 * Tombol kartu besar "Surprise Me / Resep Acak".
 *
 * Fitur visual & animasi:
 * - Idle: efek napas halus (subtle pulse scale).
 * - Saat goyang / shake terdeteksi: efek wobble rotasi berulang beberapa saat.
 * - Saat ditekan: efek scale-down ~0.97f.
 * - Saat memuat (loading): ikon berputar halus atau CircularProgressIndicator mini.
 */
@Composable
fun SurpriseMeCard(
    isLoading: Boolean,
    isShakeTriggered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animasi scale saat ditekan
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = Motion.shortTween(),
        label = "surprisePressScale"
    )

    // Animasi rotasi wobble saat shake terpicu
    val wobbleRotation = remember { Animatable(0f) }
    LaunchedEffect(isShakeTriggered) {
        if (isShakeTriggered) {
            wobbleRotation.animateTo(12f, tween(80, easing = LinearEasing))
            wobbleRotation.animateTo(-12f, tween(80, easing = LinearEasing))
            wobbleRotation.animateTo(8f, tween(80, easing = LinearEasing))
            wobbleRotation.animateTo(-8f, tween(80, easing = LinearEasing))
            wobbleRotation.animateTo(0f, tween(80, easing = LinearEasing))
        }
    }

    // Animasi idle pulse lembut saat tidak loading
    val infiniteTransition = rememberInfiniteTransition(label = "surpriseIdle")
    val idlePulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = Motion.EasingStandard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    // Animasi rotasi ikon saat loading
    val loadingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingRotate"
    )

    val currentScale = if (isLoading) 1f else pressScale * idlePulseScale

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = wobbleRotation.value
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Wadah Ikon Dadu / Sparkles
                Surface(
                    shape = CircleShape,
                    color = PrimaryOrange,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                if (isLoading) {
                                    rotationZ = loadingRotation
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Casino,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.btn_surprise_me),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLoading) {
                            stringResource(R.string.surprise_me_loading)
                        } else {
                            stringResource(R.string.surprise_me_desc)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(20.dp),
                    color = PrimaryOrange
                )
            }
        }
    }
}
