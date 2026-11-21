package com.example.resepedia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resepedia.R
import com.example.resepedia.data.AppThemeMode
import com.example.resepedia.model.MealSummary
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.UiState
import com.example.resepedia.ui.components.CategoryChip
import com.example.resepedia.ui.components.EmptyView
import com.example.resepedia.ui.components.ErrorView
import com.example.resepedia.ui.components.HeroRecipeCard
import com.example.resepedia.ui.components.LanguageSelectionDialog
import com.example.resepedia.ui.components.LoadingView
import com.example.resepedia.ui.components.RecipeCard
import com.example.resepedia.ui.components.ThemeSelectionDialog
import com.example.resepedia.ui.theme.PrimaryOrange

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val heroState by viewModel.heroRecipeState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val recipesByCategoryState by viewModel.recipesByCategoryState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResultsState by viewModel.searchResultsState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Dialog Pengaturan Tema
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = currentThemeMode,
            onSelectMode = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Dialog Pengaturan Bahasa
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = viewModel.getLanguage(),
            onSelectLanguage = { langCode ->
                viewModel.setLanguage(langCode)
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
                )
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Single LazyVerticalGrid utama (kolom tetap 2 di portrait maupun landscape)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                        Surface(
                            shape = CircleShape,
                            color = PrimaryOrange,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
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

                    // Tombol Aksi: Bahasa & Tema
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showLanguageDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = stringResource(R.string.btn_change_language),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (currentThemeMode == AppThemeMode.DARK) {
                                    Icons.Filled.DarkMode
                                } else {
                                    Icons.Filled.LightMode
                                },
                                contentDescription = stringResource(R.string.btn_change_theme),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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

        // 2. Search Bar Membulat (Span Penuh)
        item(span = { GridItemSpan(maxLineSpan) }, key = "search_section") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.search_clear_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }

        // Jika Sedang Mencari (Search Mode)
        if (searchQuery.isNotBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "search_results_header") {
                Text(
                    text = stringResource(R.string.search_results_title, searchQuery),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            when (val result = searchResultsState) {
                is UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "search_loading") {
                        LoadingView()
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "search_error") {
                        ErrorView(onRetry = { viewModel.onSearchQueryChange(searchQuery) })
                    }
                }
                is UiState.Success -> {
                    val meals = result.data
                    if (meals.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "search_empty") {
                            EmptyView(
                                title = stringResource(R.string.search_empty_title),
                                description = stringResource(R.string.search_empty_desc)
                            )
                        }
                    } else {
                        items(
                            items = meals,
                            key = { "search_${it.idMeal ?: it.hashCode()}" }
                        ) { meal ->
                            RecipeCard(
                                meal = meal,
                                isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                                onMealClick = onNavigateToDetail,
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }
                    }
                }
                null -> {}
            }
        } else {
            // 3. Hero Card "Resep Hari Ini" (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "hero_section") {
                when (val hero = heroState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingView()
                        }
                    }
                    is UiState.Error -> {
                        ErrorView(onRetry = { viewModel.loadHeroRecipe() })
                    }
                    is UiState.Success -> {
                        HeroRecipeCard(
                            meal = hero.data,
                            onMealClick = onNavigateToDetail,
                            onRefresh = { viewModel.loadHeroRecipe() }
                        )
                    }
                }
            }

            // 4. Baris Kategori yang Bisa Digeser (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "categories_section") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestaurantMenu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.categories_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (val catState = categoriesState) {
                        is UiState.Loading -> LoadingView()
                        is UiState.Error -> ErrorView(onRetry = { viewModel.loadCategories() })
                        is UiState.Success -> {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(
                                    items = catState.data,
                                    key = { it.idCategory ?: it.strCategory.orEmpty() }
                                ) { category ->
                                    CategoryChip(
                                        category = category,
                                        isSelected = category.strCategory.equals(selectedCategory, ignoreCase = true),
                                        onCategoryClick = { viewModel.selectCategory(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Judul Daftar Resep Sesuai Kategori Terpilih (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "category_recipes_title") {
                Text(
                    text = stringResource(R.string.recipes_by_category, selectedCategory),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // 6. Daftar Resep per Kategori (2 Kolom Tetap)
            when (val mealsState = recipesByCategoryState) {
                is UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "meals_loading") {
                        LoadingView()
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "meals_error") {
                        ErrorView(onRetry = { viewModel.loadMealsByCategory(selectedCategory) })
                    }
                }
                is UiState.Success -> {
                    items(
                        items = mealsState.data,
                        key = { "meal_${it.idMeal ?: it.hashCode()}" }
                    ) { meal ->
                        RecipeCard(
                            meal = meal,
                            isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                            onMealClick = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            categoryBadge = selectedCategory
                        )
                    }
                }
            }
        }
    }
}
