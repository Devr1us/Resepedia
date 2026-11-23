package com.example.resepedia.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.resepedia.R
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.UiState
import com.example.resepedia.ui.components.AreaCard
import com.example.resepedia.ui.components.AreaCardSkeleton
import com.example.resepedia.ui.components.EmptyView
import com.example.resepedia.ui.components.ErrorView
import com.example.resepedia.ui.components.RecipeCard
import com.example.resepedia.ui.components.RecipeCardSkeleton
import com.example.resepedia.ui.components.rememberShimmerBrush
import com.example.resepedia.ui.util.areaDisplayName
import com.example.resepedia.ui.util.getAreaFlag

/**
 * Layar Jelajah Negara (Area).
 * Menggunakan satu LazyVerticalGrid(GridCells.Fixed(2)) yang tetap 2 kolom di portrait maupun landscape.
 *
 * Alur Layar:
 * - Mode A (selectedArea == null): Menampilkan daftar negara dalam kartu grid (AreaCard).
 * - Mode B (selectedArea != null): Menampilkan daftar resep dari negara terpilih (RecipeCard).
 *   Tombol kembali di header maupun tombol back fisik/gesture (BackHandler) akan mengembalikan ke Mode A.
 */
@Composable
fun AreaScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.loadAreas()
    }

    val areasState by viewModel.areasState.collectAsState()
    val selectedArea by viewModel.selectedArea.collectAsState()
    val mealsByAreaState by viewModel.mealsByAreaState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val translations by viewModel.translations.collectAsState()
    val shimmerBrush = rememberShimmerBrush()

    val currentArea = selectedArea.orEmpty()
    val translatedAreaName = if (selectedArea != null) areaDisplayName(currentArea) else ""
    val flagEmoji = if (selectedArea != null) getAreaFlag(currentArea) else ""

    // Tangani tombol Back fisik/gesture saat berada di Mode B agar kembali ke Mode A
    BackHandler(enabled = selectedArea != null) {
        viewModel.clearSelectedArea()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val maxLineSpan = 2

        if (selectedArea == null) {
            // ==========================================
            // MODE A: DAFTAR NEGARA / AREA
            // ==========================================

            // Header Mode A (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "areas_header") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.areas_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.areas_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Konten State Mode A
            when (val state = areasState) {
                is UiState.Loading -> {
                    items(count = 8, key = { "skeleton_area_$it" }) {
                        AreaCardSkeleton(brush = shimmerBrush)
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "areas_error") {
                        ErrorView(
                            description = state.message ?: stringResource(R.string.error_connection_desc),
                            onRetry = { viewModel.loadAreas(forceRefresh = true) }
                        )
                    }
                }
                is UiState.Success -> {
                    val areas = state.data
                    if (areas.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "areas_empty") {
                            EmptyView(
                                title = stringResource(R.string.areas_empty_title),
                                description = stringResource(R.string.areas_empty_desc),
                                icon = Icons.Outlined.Public
                            )
                        }
                    } else {
                        items(
                            items = areas,
                            key = { "area_$it" }
                        ) { area ->
                            val flag = getAreaFlag(area)
                            val displayName = areaDisplayName(area)
                            AreaCard(
                                areaName = area,
                                displayName = displayName,
                                flagEmoji = flag,
                                onClick = { viewModel.selectArea(area) }
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // MODE B: DAFTAR RESEP DARI NEGARA TERPILIH
            // ==========================================

            // Header Mode B dengan Tombol Kembali (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "area_recipes_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.clearSelectedArea() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back_to_areas),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.areas_recipes_title, translatedAreaName),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$flagEmoji $currentArea",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Konten State Mode B
            when (val state = mealsByAreaState) {
                is UiState.Loading -> {
                    items(count = 6, key = { "skeleton_area_meal_$it" }) {
                        RecipeCardSkeleton(brush = shimmerBrush)
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "area_meals_error") {
                        ErrorView(
                            description = state.message ?: stringResource(R.string.error_connection_desc),
                            onRetry = { viewModel.loadMealsByArea(currentArea) }
                        )
                    }
                }
                is UiState.Success -> {
                    val meals = state.data
                    if (meals.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "area_meals_empty") {
                            EmptyView(
                                title = stringResource(R.string.areas_empty_title),
                                description = stringResource(R.string.areas_empty_desc)
                            )
                        }
                    } else {
                        items(
                            items = meals,
                            key = { "area_meal_${it.idMeal ?: it.hashCode()}" }
                        ) { meal ->
                            val translatedTitle = translations[meal.strMeal] ?: meal.strMeal
                            val translatedMeal = meal.copy(strMeal = translatedTitle)
                            RecipeCard(
                                meal = translatedMeal,
                                isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                                onMealClick = onNavigateToDetail,
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                categoryBadge = "$translatedAreaName $flagEmoji"
                            )
                        }
                    }
                }
            }
        }
    }
}
