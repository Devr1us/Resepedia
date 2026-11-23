package com.example.resepedia.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.resepedia.R
import com.example.resepedia.ui.util.rememberWindowInfo

/**
 * Menghasilkan Brush gradien shimmer bergerak menggunakan infinite transition tunggal.
 * Warna otomatis mengikuti MaterialTheme.colorScheme sehingga kontras dan terbaca
 * baik di Mode Terang (Light) maupun Mode Gelap (Dark).
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslation"
    )

    val isLight = MaterialTheme.colorScheme.surface.red > 0.5f
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = if (isLight) {
        Color.White.copy(alpha = 0.75f)
    } else {
        Color(0xFF383838)
    }

    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 350f, y = translateAnim - 350f),
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Modifier extension untuk mengaplikasikan efek shimmer dengan Brush yang diberikan.
 */
fun Modifier.shimmer(brush: Brush): Modifier = this.background(brush)

/**
 * Modifier extension mandiri untuk mengaplikasikan efek shimmer langsung.
 */
@Composable
fun Modifier.shimmer(): Modifier = this.background(rememberShimmerBrush())

/**
 * Skeleton untuk HeroRecipeCard "Resep Hari Ini" di Dashboard.
 * Tinggi 240.dp dan sudut 24.dp agar identik dengan kartu aslinya (mencegah layout jump).
 */
@Composable
fun HeroSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight
    val skeletonHeight = if (isCompact) 175.dp else 240.dp

    val loadingDesc = stringResource(R.string.loading_desc)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(skeletonHeight)
            .semantics { contentDescription = loadingDesc },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmer(brush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 12.dp else 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge placeholder (Resep Hari Ini)
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                )

                // Judul & area placeholder
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

/**
 * Skeleton untuk kartu resep (RecipeCard).
 * Sudut 20.dp, blok gambar aspectRatio(1.15f), dan 2 baris teks (lebar 90% dan 60%).
 */
@Composable
fun RecipeCardSkeleton(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Blok Gambar aspect ratio 1.15f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .shimmer(brush)
            )

            // Area 2 baris teks placeholder
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Baris teks 1 (lebar 90%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Baris teks 2 (lebar 60%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(brush)
                )
            }
        }
    }
}

/**
 * Skeleton untuk chip kategori individual (CategoryChip).
 */
@Composable
fun SingleCategoryChipSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(76.dp)
            .padding(vertical = 4.dp)
    ) {
        // Lingkaran gambar kategori (60.dp)
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .shimmer(brush)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Label nama kategori
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(brush)
        )
    }
}

/**
 * Skeleton untuk baris horizontal chip kategori (CategoryChip).
 */
@Composable
fun CategoryChipSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush(),
    count: Int = 6
) {
    val loadingDesc = stringResource(R.string.loading_desc)
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = loadingDesc },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(count) {
            SingleCategoryChipSkeleton(brush = brush)
        }
    }
}

/**
 * Skeleton untuk layar Detail resep (DetailScreen).
 * Meniru struktur visual gambar penuh, judul, chip info, daftar bahan, dan langkah memasak.
 */
@Composable
fun DetailSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    val windowInfo = rememberWindowInfo()
    val isLandscape = windowInfo.isLandscape
    val loadingDesc = stringResource(R.string.loading_desc)

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { contentDescription = loadingDesc }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kolom Kiri Skeleton
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shimmer(brush)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(brush)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer(brush)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer(brush)
                    )
                }
            }

            // Kolom Kanan Skeleton
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(brush)
                )
                Spacer(modifier = Modifier.height(12.dp))
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer(brush)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(brush)
                )
                Spacer(modifier = Modifier.height(12.dp))
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmer(brush)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { contentDescription = loadingDesc }
        ) {
            // 1. Gambar Utama (Header aspect ratio 1.25f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .shimmer(brush)
            )

            // 2. Konten Rincian
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Placeholder Judul resep
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(brush)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Placeholder Chip Kategori & Asal Negara
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 85.dp, height = 30.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmer(brush)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 85.dp, height = 30.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmer(brush)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Placeholder Judul Bagian Bahan
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(brush)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Placeholder Item Bahan (3 baris)
                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer(brush)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer(brush)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Placeholder Judul Langkah Memasak
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(brush)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Placeholder Kartu Langkah Memasak (2 kartu)
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmer(brush)
                    )
                }
            }
        }
    }
}
