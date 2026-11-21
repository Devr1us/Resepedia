package com.example.resepedia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.resepedia.R
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.UiState
import com.example.resepedia.ui.components.EmptyView
import com.example.resepedia.ui.components.ErrorView
import com.example.resepedia.ui.components.LoadingView
import com.example.resepedia.ui.components.RecipeCard

@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.loadFavoriteMeals()
    }

    val favoritesState by viewModel.favoritesState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Single LazyVerticalGrid utama dengan 2 Kolom Tetap
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Layar Favorit (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "favorites_header") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.nav_favorite),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            when (val state = favoritesState) {
                is UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "fav_loading") {
                        LoadingView()
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "fav_error") {
                        ErrorView(onRetry = { viewModel.loadFavoriteMeals() })
                    }
                }
                is UiState.Success -> {
                    val meals = state.data
                    if (meals.isEmpty() || favoriteIds.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "fav_empty") {
                            EmptyView(
                                title = stringResource(R.string.favorites_empty_title),
                                description = stringResource(R.string.favorites_empty_desc),
                                icon = Icons.Outlined.FavoriteBorder
                            )
                        }
                    } else {
                        items(
                            items = meals,
                            key = { "fav_${it.idMeal ?: it.hashCode()}" }
                        ) { meal ->
                            RecipeCard(
                                meal = meal,
                                isFavorite = true,
                                onMealClick = onNavigateToDetail,
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
