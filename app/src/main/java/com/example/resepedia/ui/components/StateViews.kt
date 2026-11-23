package com.example.resepedia.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.resepedia.R
import com.example.resepedia.ui.theme.Motion
import com.example.resepedia.ui.util.rememberWindowInfo
import kotlinx.coroutines.launch

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(if (isCompact) 16.dp else 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(if (isCompact) 32.dp else 42.dp)
            )
            Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))
            Text(
                text = stringResource(R.string.loading_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_connection_title),
    description: String = stringResource(R.string.error_connection_desc),
    icon: ImageVector = Icons.Outlined.WifiOff
) {
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    // Animasi kemunculan halus (fade + skala kecil)
    val animScale = remember { Animatable(0.92f) }
    val animAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { animScale.animateTo(1f, animationSpec = Motion.mediumTween()) }
        launch { animAlpha.animateTo(1f, animationSpec = Motion.mediumTween()) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animScale.value
                scaleY = animScale.value
                alpha = animAlpha.value
            }
            .padding(if (isCompact) 12.dp else 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(if (isCompact) 36.dp else 54.dp)
            )
            Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 16.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = stringResource(R.string.btn_retry))
            }
        }
    }
}

@Composable
fun EmptyView(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.SearchOff
) {
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    // Animasi kemunculan halus (fade + skala kecil)
    val animScale = remember { Animatable(0.92f) }
    val animAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { animScale.animateTo(1f, animationSpec = Motion.mediumTween()) }
        launch { animAlpha.animateTo(1f, animationSpec = Motion.mediumTween()) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animScale.value
                scaleY = animScale.value
                alpha = animAlpha.value
            }
            .padding(if (isCompact) 16.dp else 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(if (isCompact) 36.dp else 56.dp)
            )
            Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
