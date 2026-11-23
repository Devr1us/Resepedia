package com.example.resepedia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.example.resepedia.R
import com.example.resepedia.ui.theme.Motion

/**
 * Kartu Negara / Area masakan untuk layar Jelajah Negara.
 * Mengikuti gaya desain RecipeCard (sudut 20.dp, elevasi 2.dp, warna surface).
 * Mendukung status ketersediaan resep:
 * - Memiliki resep: Tampil cerah dengan jumlah resep ("12 resep").
 * - Tanpa resep: Tampil redup (alpha 0.68f) dengan badge "Belum ada resep".
 */
@Composable
fun AreaCard(
    areaName: String,
    displayName: String,
    flagEmoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    recipeCount: Int? = null,
    flagUrl: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = Motion.shortTween(),
        label = "areaCardPressScale"
    )

    val isEmptyArea = recipeCount != null && recipeCount == 0

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                alpha = if (isEmptyArea) 0.68f else 1f
            }
            .clip(RoundedCornerShape(20.dp))
            .semantics {
                contentDescription = "$displayName, $flagEmoji"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEmptyArea) 0.5.dp else 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            // Bendera Negara (FlagCDN via Coil dengan emoji sebagai fallback instan)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(height = 36.dp, width = 50.dp)
            ) {
                if (!flagUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = flagUrl,
                        contentDescription = displayName,
                        modifier = Modifier
                            .size(width = 46.dp, height = 30.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Text(
                                text = flagEmoji,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        error = {
                            Text(
                                text = flagEmoji,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                } else {
                    Text(
                        text = flagEmoji,
                        fontSize = 34.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama Negara / Area
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Status Jumlah Resep / Badge Belum Ada Resep
            if (recipeCount != null) {
                if (recipeCount > 0) {
                    Text(
                        text = stringResource(R.string.area_recipe_count, recipeCount),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.area_no_recipes),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // Placeholder tipis agar tinggi kartu tetap stabil saat data count sedang di-fetch
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

/**
 * Skeleton shimmer untuk kartu AreaCard saat daftar negara sedang dimuat.
 */
@Composable
fun AreaCardSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    val loadingDesc = stringResource(R.string.loading_desc)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .semantics { contentDescription = loadingDesc },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            // Placeholder Lingkaran Bendera
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .shimmer(brush)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Placeholder Baris Nama Negara
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(brush)
            )
        }
    }
}
