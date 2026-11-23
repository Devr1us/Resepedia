package com.example.resepedia.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.resepedia.ui.theme.staggeredEntrance
import com.example.resepedia.ui.util.AreaFlags
import com.example.resepedia.ui.util.getAreaDisplayName
import com.example.resepedia.ui.util.getAreaFlag
import com.example.resepedia.ui.util.rememberWindowInfo
import com.example.resepedia.ui.util.responsiveGridCells
import java.text.Normalizer
import java.util.Locale

/**
 * Layar Jelajah Negara (Area / Cuisine).
 * Menggunakan satu LazyVerticalGrid dengan GridCells adaptif yang responsif.
 *
 * Alur Layar:
 * - Mode A (selectedArea == null): Daftar seluruh negara dengan search bar lokal, pengelompokan
 *   negara yang memiliki resep di bagian atas, dan badge resep.
 * - Mode B (selectedArea != null): Daftar resep dari negara terpilih dengan search bar lokal.
 *   Bila negara belum memiliki resep di database, menampilkan EmptyView informatif dengan tombol
 *   resep acak dan tombol jelajahi negara lain.
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

    val windowInfo = rememberWindowInfo()
    val isLandscape = windowInfo.isLandscape
    val isCompactHeight = windowInfo.isCompactHeight

    val areasState by viewModel.areasState.collectAsState()
    val selectedArea by viewModel.selectedArea.collectAsState()
    val mealsByAreaState by viewModel.mealsByAreaState.collectAsState()
    val recipeCounts by viewModel.areaRecipeCounts.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val translations by viewModel.translations.collectAsState()
    val isSurpriseLoading by viewModel.isSurpriseLoading.collectAsState()
    val shimmerBrush = rememberShimmerBrush()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // State query pencarian real-time (bertahan saat rotasi layar)
    var areaSearchQuery by rememberSaveable { mutableStateOf("") }
    var mealSearchQuery by rememberSaveable { mutableStateOf("") }

    // Reset query resep saat berpindah negara atau kembali ke Mode A
    LaunchedEffect(selectedArea) {
        mealSearchQuery = ""
    }

    // State untuk memastikan animasi staggered entrance hanya diputar sekali saat layar dibuka
    var hasAnimatedEntrance by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasAnimatedEntrance = true
    }

    val currentArea = selectedArea.orEmpty()
    val translatedAreaName = if (selectedArea != null) getAreaDisplayName(currentArea, context) else ""
    val flagEmoji = if (selectedArea != null) getAreaFlag(currentArea) else ""

    // Tangani tombol Back fisik/gesture saat berada di Mode B agar kembali ke Mode A
    BackHandler(enabled = selectedArea != null) {
        viewModel.clearSelectedArea()
    }

    // Persiapan data untuk Mode A (Daftar Negara)
    val areas = (areasState as? UiState.Success)?.data.orEmpty()
    val sortedAreas = remember(areas, recipeCounts, context) {
        areas.sortedWith(
            compareBy<String> { area ->
                val count = recipeCounts[area]
                if (count != null && count == 0) 1 else 0
            }.thenBy { area ->
                normalizeText(getAreaDisplayName(area, context))
            }
        )
    }

    val filteredAreas = remember(sortedAreas, areaSearchQuery, context) {
        if (areaSearchQuery.isBlank()) {
            sortedAreas
        } else {
            val cleanQuery = normalizeText(areaSearchQuery)
            sortedAreas.filter { area ->
                val displayName = getAreaDisplayName(area, context)
                normalizeText(area).contains(cleanQuery) ||
                    normalizeText(displayName).contains(cleanQuery)
            }
        }
    }

    // Persiapan data untuk Mode B (Daftar Resep Negara Terpilih)
    val meals = (mealsByAreaState as? UiState.Success)?.data.orEmpty()
    val filteredMeals = remember(meals, mealSearchQuery, translations) {
        if (mealSearchQuery.isBlank()) {
            meals
        } else {
            val cleanQuery = normalizeText(mealSearchQuery)
            meals.filter { meal ->
                val original = meal.strMeal.orEmpty()
                val translated = translations[original] ?: original
                normalizeText(original).contains(cleanQuery) ||
                    normalizeText(translated).contains(cleanQuery)
            }
        }
    }

    LazyVerticalGrid(
        columns = responsiveGridCells(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (selectedArea == null) {
            // ==========================================
            // MODE A: DAFTAR NEGARA / AREA
            // ==========================================

            // 1. Header Mode A (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "areas_header") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.areas_title),
                        style = if (isCompactHeight) {
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        },
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 2.dp else 4.dp))
                    Text(
                        text = stringResource(R.string.areas_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Search Field Real-time untuk Negara / Cuisine (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "areas_search_bar") {
                AreaSearchBar(
                    query = areaSearchQuery,
                    onQueryChange = { areaSearchQuery = it },
                    placeholder = stringResource(R.string.areas_search_placeholder),
                    onClearQuery = {
                        areaSearchQuery = ""
                        focusManager.clearFocus()
                    },
                    onSearchAction = { focusManager.clearFocus() }
                )
            }

            // 3. Konten State Mode A
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
                    if (filteredAreas.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "areas_search_empty") {
                            EmptyView(
                                title = stringResource(R.string.areas_search_empty_title),
                                description = stringResource(R.string.areas_search_empty_desc),
                                icon = Icons.Outlined.Public
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = filteredAreas,
                            key = { _, it -> "area_$it" }
                        ) { index, area ->
                            val flag = getAreaFlag(area)
                            val displayName = getAreaDisplayName(area, context)
                            val count = recipeCounts[area]
                            val flagUrl = AreaFlags.getFlagUrl(area)

                            AreaCard(
                                areaName = area,
                                displayName = displayName,
                                flagEmoji = flag,
                                recipeCount = count,
                                flagUrl = flagUrl,
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.selectArea(area)
                                },
                                modifier = Modifier
                                    .animateItem()
                                    .staggeredEntrance(
                                        index = index,
                                        enabled = !hasAnimatedEntrance
                                    )
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // MODE B: DAFTAR RESEP DARI NEGARA TERPILIH
            // ==========================================

            // 1. Header Mode B dengan Tombol Kembali (Span Penuh)
            item(span = { GridItemSpan(maxLineSpan) }, key = "area_recipes_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.clearSelectedArea()
                        },
                        modifier = Modifier
                            .size(if (isCompactHeight) 36.dp else 40.dp)
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
                            style = if (isCompactHeight) {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            },
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

            // 2. Konten State Mode B
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
                            onRetry = { viewModel.loadMealsByArea(currentArea, forceRefresh = true) }
                        )
                    }
                }
                is UiState.Success -> {
                    if (meals.isEmpty()) {
                        // Tampilan informatif khusus negara yang belum memiliki resep di database
                        item(span = { GridItemSpan(maxLineSpan) }, key = "empty_country_recipes") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = if (isCompactHeight) 8.dp else 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 520.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(if (isCompactHeight) 16.dp else 24.dp)
                                    ) {
                                        Text(
                                            text = flagEmoji,
                                            fontSize = if (isCompactHeight) 38.sp else 48.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 12.dp))
                                        Text(
                                            text = stringResource(R.string.areas_empty_country_title, translatedAreaName),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.areas_empty_country_desc),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 20.dp))

                                        // Tombol Aksi: Bersisian di Landscape (Row dengan weight 1f), Tumpuk di Portrait (Column)
                                        if (isLandscape) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = { viewModel.clearSelectedArea() },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(46.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Public,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = stringResource(R.string.areas_btn_other_countries),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.fetchRandomMeal(
                                                            onSuccess = onNavigateToDetail,
                                                            onError = { /* abaikan */ }
                                                        )
                                                    },
                                                    enabled = !isSurpriseLoading,
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(46.dp)
                                                ) {
                                                    if (isSurpriseLoading) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(18.dp),
                                                            strokeWidth = 2.dp,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = stringResource(R.string.surprise_me_loading),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Filled.Casino,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = stringResource(R.string.areas_btn_surprise_me),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.fetchRandomMeal(
                                                            onSuccess = onNavigateToDetail,
                                                            onError = { /* abaikan */ }
                                                        )
                                                    },
                                                    enabled = !isSurpriseLoading,
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                ) {
                                                    if (isSurpriseLoading) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(18.dp),
                                                            strokeWidth = 2.dp,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(text = stringResource(R.string.surprise_me_loading))
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Filled.Casino,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = stringResource(R.string.areas_btn_surprise_me),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { viewModel.clearSelectedArea() },
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Public,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = stringResource(R.string.areas_btn_other_countries),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // 2a. Search Bar Penyaring Resep di Negara Terpilih (Span Penuh)
                        item(span = { GridItemSpan(maxLineSpan) }, key = "meal_search_bar") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                AreaSearchBar(
                                    query = mealSearchQuery,
                                    onQueryChange = { mealSearchQuery = it },
                                    placeholder = stringResource(R.string.areas_recipe_search_placeholder),
                                    onClearQuery = {
                                        mealSearchQuery = ""
                                        focusManager.clearFocus()
                                    },
                                    onSearchAction = { focusManager.clearFocus() }
                                )

                                if (mealSearchQuery.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(
                                            R.string.areas_recipe_search_results_count,
                                            filteredMeals.size
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        if (filteredMeals.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }, key = "recipe_search_empty") {
                                EmptyView(
                                    title = stringResource(R.string.search_empty_title),
                                    description = stringResource(R.string.search_empty_desc)
                                )
                            }
                        } else {
                            itemsIndexed(
                                items = filteredMeals,
                                key = { _, it -> "area_meal_${it.idMeal ?: it.hashCode()}" }
                            ) { index, meal ->
                                val translatedTitle = translations[meal.strMeal] ?: meal.strMeal
                                val translatedMeal = meal.copy(strMeal = translatedTitle)
                                RecipeCard(
                                    meal = translatedMeal,
                                    isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                                    onMealClick = onNavigateToDetail,
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    categoryBadge = "$translatedAreaName $flagEmoji",
                                    modifier = Modifier
                                        .animateItem()
                                        .staggeredEntrance(
                                            index = index,
                                            enabled = !hasAnimatedEntrance
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Search bar elegan untuk penyaringan negara maupun resep di dalam halaman Area.
 */
@Composable
private fun AreaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClearQuery: () -> Unit,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(if (isCompact) 46.dp else 52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.search_clear_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper untuk normalisasi teks: menghapus tanda diakritik, spasi berlebih, dan huruf kecil.
 */
private fun normalizeText(text: String): String {
    val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex()
        .replace(normalized, "")
        .trim()
        .lowercase(Locale.ROOT)
}
