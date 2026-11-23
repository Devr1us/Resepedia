package com.example.resepedia.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resepedia.R

/**
 * Kartu Negara / Area masakan untuk layar Jelajah Negara.
 * Mengikuti gaya desain RecipeCard (sudut 20.dp, elevasi 2.dp, warna surface).
 */
@Composable
fun AreaCard(
    areaName: String,
    displayName: String,
    flagEmoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$displayName, $flagEmoji"
            },
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
            // Emoji Bendera Berukuran Besar
            Text(
                text = flagEmoji,
                fontSize = 38.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

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
