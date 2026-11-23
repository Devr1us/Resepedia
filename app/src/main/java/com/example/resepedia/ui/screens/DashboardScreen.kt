package com.example.resepedia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resepedia.R
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.UiState
import com.example.resepedia.ui.components.AppLogo
import com.example.resepedia.ui.components.CategoryChip
import com.example.resepedia.ui.components.CategoryChipSkeleton
import com.example.resepedia.ui.components.ErrorView
import com.example.resepedia.ui.components.HeroRecipeCard
import com.example.resepedia.ui.components.HeroSkeleton
import com.example.resepedia.ui.components.RecipeCard
import com.example.resepedia.ui.components.RecipeCardSkeleton
import com.example.resepedia.ui.components.SurpriseMeCard
import com.example.resepedia.ui.components.rememberShimmerBrush
import com.example.resepedia.ui.theme.PrimaryOrange
import com.example.resepedia.ui.theme.staggeredEntrance
import com.example.resepedia.ui.util.ShakeEffect
import com.example.resepedia.ui.util.responsiveGridCells
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Layar Utama (Dashboard / Beranda):
 * - Header: Logo, Tagline, dan tombol ke Layar Pengaturan.
 * - Search Bar membulat sebagai pintu masuk cepat ke Layar Pencarian Khusus.
 * - Tombol Kartu "Surprise Me": resep acak instan via ketukan atau goyangan perangkat (Shake).
 * - Hero Card: Resep Hari Ini dengan tombol refresh.
 * - Kategori Masakan: Baris horizontal interaktif dengan auto-scroll ke kategori terpilih.
 * - Daftar Resep per Kategori: Grid 2 kolom tetap.
 */
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroState by viewModel.heroRecipeState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val recipesByCategoryState by viewModel.recipesByCategoryState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val translations by viewModel.translations.collectAsState()
    val shimmerBrush = rememberShimmerBrush()

    // State untuk fitur Surprise Me & Shake
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isSurpriseLoading by viewModel.isSurpriseLoading.collectAsState()
    val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()
    val haptic = LocalHapticFeedback.current
    var isShakeTriggered by rememberSaveable { mutableStateOf(false) }

    fun triggerSurpriseMe() {
        viewModel.fetchRandomMeal(
            onSuccess = { mealId ->
                onNavigateToDetail(mealId)
            },
            onError = { errorMsg ->
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = errorMsg,
                        actionLabel = "Coba Lagi"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        triggerSurpriseMe()
                    }
                }
            }
        )
    }

    // Listener goyangan (Shake) dengan haptic feedback
    ShakeEffect(enabled = isShakeEnabled) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isShakeTriggered = true
        coroutineScope.launch {
            delay(400)
            isShakeTriggered = false
        }
        triggerSurpriseMe()
    }

    // Penanda animasi bertahap awal hanya berjalan sekali per kunjungan layar
    var hasAnimatedEntrance by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasAnimatedEntrance = true
    }

    val categoryListState = rememberLazyListState()
    LaunchedEffect(selectedCategory) {
        val catData = (categoriesState as? UiState.Success)?.data
        if (catData != null) {
            val idx = catData.indexOfFirst { it.strCategory.equals(selectedCategory, ignoreCase = true) }
            if (idx >= 0) {
                categoryListState.animateScrollToItem(idx)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Single LazyVerticalGrid utama (kolom adaptif responsif di portrait & landscape/tablet)
        LazyVerticalGrid(
            columns = responsiveGridCells(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header (Span Penuh): Logo, Nama Aplikasi, Tombol Bahasa & Tema
            item(span = { GridItemSpan(maxLineSpan) }, key = "header_section") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Logo & Brand Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppLogo(size = 42.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.tagline),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Tombol Pengaturan (Gerigi: 38.dp, bulat, latar surfaceVariant, tint primary)
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.btn_open_settings_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sapaan Ramah
                    Text(
                        text = stringResource(R.string.greeting_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 2. Search Bar Membulat sebagai Pintu Masuk ke Layar Pencarian Khusus (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "search_section") {
                Surface(
                    onClick = onNavigateToSearch,
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Tombol Kartu "Surprise Me" / Resep Acak (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "surprise_me_section") {
                SurpriseMeCard(
                    isLoading = isSurpriseLoading,
                    isShakeTriggered = isShakeTriggered,
                    onClick = { triggerSurpriseMe() }
                )
            }

            // 4. Hero Card "Resep Hari Ini" (Span Penuh dengan data terjemahan bila tersedia)
            item(span = { GridItemSpan(maxLineSpan) }, key = "hero_section") {
                when (val hero = heroState) {
                    is UiState.Loading -> {
                        HeroSkeleton(brush = shimmerBrush)
                    }
                    is UiState.Error -> {
                        ErrorView(onRetry = { viewModel.loadHeroRecipe() })
                    }
                    is UiState.Success -> {
                        val heroMeal = hero.data
                        val translatedHero = heroMeal.copy(
                            strMeal = translations[heroMeal.strMeal] ?: heroMeal.strMeal,
                            strCategory = translations[heroMeal.strCategory] ?: heroMeal.strCategory,
                            strArea = translations[heroMeal.strArea] ?: heroMeal.strArea
                        )
                        HeroRecipeCard(
                            meal = translatedHero,
                            onMealClick = onNavigateToDetail,
                            onRefresh = { viewModel.loadHeroRecipe() }
                        )
                    }
                }
            }

            // 5. Baris Kategori yang Bisa Digeser (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "categories_section") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.categories_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (val catState = categoriesState) {
                        is UiState.Loading -> {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(count = 5) {
                                    CategoryChipSkeleton(brush = shimmerBrush)
                                }
                            }
                        }
                        is UiState.Error -> {
                            ErrorView(onRetry = { viewModel.loadCategories() })
                        }
                        is UiState.Success -> {
                            val categories = catState.data
                            LazyRow(
                                state = categoryListState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(
                                    items = categories,
                                    key = { it.idCategory ?: it.strCategory ?: "" }
                                ) { category ->
                                    val catName = category.strCategory.orEmpty()
                                    val translatedName = translations[catName] ?: catName
                                    CategoryChip(
                                        modifier = Modifier.animateItem(),
                                        category = category,
                                        displayName = translatedName,
                                        isSelected = catName.equals(selectedCategory, ignoreCase = true),
                                        onCategoryClick = { viewModel.selectCategory(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Header Bagian Resep Berdasarkan Kategori (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "recipes_header") {
                val translatedCategory = translations[selectedCategory] ?: selectedCategory
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestaurantMenu,
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.recipes_by_category, translatedCategory),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 7. Daftar Resep per Kategori (2 Kolom Tetap)
            when (val mealsState = recipesByCategoryState) {
                is UiState.Loading -> {
                    items(count = 6, key = { "skeleton_recipe_$it" }) {
                        RecipeCardSkeleton(brush = shimmerBrush)
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "meals_error") {
                        ErrorView(onRetry = { viewModel.loadMealsByCategory(selectedCategory) })
                    }
                }
                is UiState.Success -> {
                    val translatedBadge = translations[selectedCategory] ?: selectedCategory
                    itemsIndexed(
                        items = mealsState.data,
                        key = { _, it -> "meal_${it.idMeal ?: it.hashCode()}" }
                    ) { index, meal ->
                        val translatedMeal = meal.copy(
                            strMeal = translations[meal.strMeal] ?: meal.strMeal
                        )
                        RecipeCard(
                            modifier = Modifier
                                .animateItem()
                                .staggeredEntrance(index = index, enabled = !hasAnimatedEntrance),
                            meal = translatedMeal,
                            isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                            onMealClick = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            categoryBadge = translatedBadge
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
