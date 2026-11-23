package com.example.resepedia.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.resepedia.R
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.SearchTabMode
import com.example.resepedia.ui.UiState
import com.example.resepedia.ui.components.EmptyView
import com.example.resepedia.ui.components.ErrorView
import com.example.resepedia.ui.components.RecipeCard
import com.example.resepedia.ui.components.RecipeCardSkeleton
import com.example.resepedia.ui.components.SurpriseMeCard
import com.example.resepedia.ui.components.rememberShimmerBrush
import com.example.resepedia.ui.theme.Motion
import com.example.resepedia.ui.theme.PrimaryOrange
import com.example.resepedia.ui.theme.staggeredEntrance
import com.example.resepedia.ui.util.ShakeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val POPULAR_SEARCH_NAMES = listOf(
    "Chicken", "Beef", "Pasta", "Soup", "Cake",
    "Curry", "Fried Rice", "Salmon", "Pancakes", "Lasagne"
)

private val POPULAR_INGREDIENTS = listOf(
    "Chicken", "Egg", "Garlic", "Onion", "Tomato",
    "Potato", "Rice", "Beef", "Salmon", "Butter"
)

private val ALPHABET_LETTERS = ('A'..'Z').toList()

/**
 * Layar Pencarian Khusus (SearchScreen):
 * - Top Bar: Tombol Kembali + Kolom Pencarian Membulat dengan auto-focus + Tombol Clear (X).
 * - Tab Mode: "Nama" (Name) dan "Bahan" (Ingredients) dengan transisi animasi halus.
 * - Mode Nama:
 *   a) Query kosong: Riwayat pencarian (max 10), Pencarian populer, Filter huruf A-Z, & Tombol Surprise Me.
 *   b) Query terisi: Debounce 400ms, Grid hasil (2 kolom) dengan Shimmer, ErrorView, & EmptyView.
 * - Mode Bahan:
 *   a) Autocomplete saran bahan (max 8) dari list.php?i=list.
 *   b) Chip bahan terpilih (max 5) atau bahan populer bila belum ada pilihan.
 *   c) Toggle AND ("Semua bahan") / OR ("Salah satu bahan") saat bahan terpilih >= 2.
 *   d) Grid resep hasil pencarian multi-bahan secara paralel di Dispatchers.IO.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (mealId: String, ingredientsParam: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchTabMode by viewModel.searchTabMode.collectAsState()
    val nameQuery by viewModel.nameSearchQuery.collectAsState()
    val selectedLetter by viewModel.selectedLetter.collectAsState()
    val nameSearchResults by viewModel.nameSearchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    val ingredientQuery by viewModel.ingredientQuery.collectAsState()
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val selectedIngredients by viewModel.selectedIngredients.collectAsState()
    val isAllIngredientsMode by viewModel.isAllIngredientsMode.collectAsState()
    val ingredientSearchResults by viewModel.ingredientSearchResults.collectAsState()
    val ingredientLimitReached by viewModel.ingredientLimitMessage.collectAsState()

    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val translations by viewModel.translations.collectAsState()
    val isSurpriseLoading by viewModel.isSurpriseLoading.collectAsState()
    val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val shimmerBrush = rememberShimmerBrush()
    var isShakeTriggered by remember { mutableStateOf(false) }

    // Auto-focus kolom pencarian saat layar dibuka pertama kali
    LaunchedEffect(Unit) {
        viewModel.loadAllIngredients()
        delay(250)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Abaikan jika focus requester belum siap
        }
    }

    // Tampilkan pesan limit 5 bahan jika pengguna mencoba menambah bahan ke-6
    LaunchedEffect(ingredientLimitReached) {
        if (ingredientLimitReached) {
            snackbarHostState.showSnackbar("Maksimal 5 bahan terpilih")
            viewModel.resetIngredientLimitMessage()
        }
    }

    fun triggerSurpriseMe() {
        viewModel.fetchRandomMeal(
            onSuccess = { mealId ->
                onNavigateToDetail(mealId, null)
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

    // Shake listener di SearchScreen (Hanya aktif saat layar tampak)
    ShakeEffect(enabled = isShakeEnabled) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isShakeTriggered = true
        coroutineScope.launch {
            delay(400)
            isShakeTriggered = false
        }
        triggerSurpriseMe()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // 1. TOP BAR: TOMBOL KEMBALI + SEARCH FIELD
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                val isNameMode = searchTabMode == SearchTabMode.NAME
                val currentText = if (isNameMode) nameQuery else ingredientQuery
                val currentPlaceholder = if (isNameMode) {
                    stringResource(R.string.search_placeholder_name)
                } else {
                    stringResource(R.string.search_placeholder_ingredient)
                }

                OutlinedTextField(
                    value = currentText,
                    onValueChange = {
                        if (isNameMode) {
                            viewModel.onNameQueryChange(it)
                        } else {
                            viewModel.onIngredientQueryChange(it)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = currentPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (currentText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (isNameMode) {
                                        viewModel.clearNameSearch()
                                    } else {
                                        viewModel.clearIngredientSearch()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.search_clear_desc),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            if (isNameMode && currentText.isNotBlank()) {
                                viewModel.submitNameSearch(currentText)
                            }
                        }
                    )
                )
            }

            // ==========================================
            // 2. TAB / SEGMENTED CONTROL: NAMA & BAHAN
            // ==========================================
            PrimaryTabRow(
                selectedTabIndex = if (searchTabMode == SearchTabMode.NAME) 0 else 1,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = searchTabMode == SearchTabMode.NAME,
                    onClick = { viewModel.setSearchTabMode(SearchTabMode.NAME) },
                    text = {
                        Text(
                            text = stringResource(R.string.search_tab_name),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
                Tab(
                    selected = searchTabMode == SearchTabMode.INGREDIENTS,
                    onClick = { viewModel.setSearchTabMode(SearchTabMode.INGREDIENTS) },
                    text = {
                        Text(
                            text = stringResource(R.string.search_tab_ingredient),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }

            // ==========================================
            // 3. KONTEN BERDASARKAN TAB MODE
            // ==========================================
            AnimatedContent(
                targetState = searchTabMode,
                transitionSpec = {
                    (fadeIn(Motion.mediumTween()) + slideInVertically(
                        initialOffsetY = { 20 },
                        animationSpec = Motion.mediumTween()
                    )) togetherWith (fadeOut(Motion.shortTween()) + slideOutVertically(
                        targetOffsetY = { -20 },
                        animationSpec = Motion.shortTween()
                    ))
                },
                label = "searchTabModeContent",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { mode ->
                when (mode) {
                    SearchTabMode.NAME -> {
                        NameModeContent(
                            viewModel = viewModel,
                            nameQuery = nameQuery,
                            selectedLetter = selectedLetter,
                            nameSearchResults = nameSearchResults,
                            searchHistory = searchHistory,
                            favoriteIds = favoriteIds,
                            translations = translations,
                            shimmerBrush = shimmerBrush,
                            isSurpriseLoading = isSurpriseLoading,
                            isShakeTriggered = isShakeTriggered,
                            onTriggerSurprise = { triggerSurpriseMe() },
                            onNavigateToDetail = { mealId ->
                                if (nameQuery.isNotBlank()) {
                                    viewModel.addSearchHistory(nameQuery)
                                }
                                onNavigateToDetail(mealId, null)
                            }
                        )
                    }
                    SearchTabMode.INGREDIENTS -> {
                        IngredientsModeContent(
                            viewModel = viewModel,
                            ingredientQuery = ingredientQuery,
                            autocompleteSuggestions = autocompleteSuggestions,
                            selectedIngredients = selectedIngredients,
                            isAllIngredientsMode = isAllIngredientsMode,
                            ingredientSearchResults = ingredientSearchResults,
                            favoriteIds = favoriteIds,
                            translations = translations,
                            shimmerBrush = shimmerBrush,
                            onNavigateToDetail = { mealId ->
                                val joined = selectedIngredients.joinToString(",")
                                onNavigateToDetail(mealId, joined)
                            }
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

/**
 * Konten Pencarian Mode "NAMA"
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NameModeContent(
    viewModel: MainViewModel,
    nameQuery: String,
    selectedLetter: Char?,
    nameSearchResults: UiState<List<com.example.resepedia.model.MealSummary>>?,
    searchHistory: List<String>,
    favoriteIds: Set<String>,
    translations: Map<String, String>,
    shimmerBrush: androidx.compose.ui.graphics.Brush,
    isSurpriseLoading: Boolean,
    isShakeTriggered: Boolean,
    onTriggerSurprise: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val isSearching = nameQuery.isNotBlank() || selectedLetter != null

    var hasAnimatedEntrance by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasAnimatedEntrance = true
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val maxLineSpan = 2

        // Filter Huruf A-Z (Selalu tampil di atas agar mudah diakses dan dibatalkan)
        item(span = { GridItemSpan(maxLineSpan) }, key = "letter_filter_section") {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = stringResource(R.string.search_browse_letter_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = ALPHABET_LETTERS, key = { it }) { letter ->
                        val isSelected = selectedLetter == letter
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.12f else 1f,
                            animationSpec = Motion.shortTween(),
                            label = "letterScale"
                        )
                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = Motion.shortTween(),
                            label = "letterColor"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = Motion.shortTween(),
                            label = "letterTextColor"
                        )

                        Surface(
                            shape = CircleShape,
                            color = containerColor,
                            modifier = Modifier
                                .size(38.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(CircleShape)
                                .clickable { viewModel.selectLetter(letter) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = letter.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                                    ),
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isSearching) {
            // --- KONDISI AWAL (QUERY KOSONG) ---

            // A. Tombol Surprise Me
            item(span = { GridItemSpan(maxLineSpan) }, key = "name_surprise_card") {
                SurpriseMeCard(
                    isLoading = isSurpriseLoading,
                    isShakeTriggered = isShakeTriggered,
                    onClick = onTriggerSurprise
                )
            }

            // B. Riwayat Pencarian (Jika ada)
            if (searchHistory.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "history_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.search_history_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        TextButton(onClick = { viewModel.clearSearchHistory() }) {
                            Text(
                                text = stringResource(R.string.search_history_clear_all),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Item Riwayat Pencarian
                items(
                    items = searchHistory,
                    key = { "history_$it" }
                ) { itemQuery ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .clickable { viewModel.submitNameSearch(itemQuery) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = itemQuery,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.removeSearchHistory(itemQuery) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // C. Pencarian Populer (Statis)
            item(span = { GridItemSpan(maxLineSpan) }, key = "popular_searches_section") {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = stringResource(R.string.search_popular_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        POPULAR_SEARCH_NAMES.forEach { popularName ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.submitNameSearch(popularName) }
                            ) {
                                Text(
                                    text = popularName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- KONDISI SEDANG MENCARI (HASIL DITAMPILKAN) ---

            // Header Jumlah Hasil
            item(span = { GridItemSpan(maxLineSpan) }, key = "name_results_header") {
                val count = (nameSearchResults as? UiState.Success)?.data?.size ?: 0
                val title = if (selectedLetter != null) {
                    "Resep berawalan huruf '$selectedLetter'"
                } else {
                    stringResource(R.string.search_results_title, nameQuery)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (nameSearchResults is UiState.Success) {
                        AnimatedContent(
                            targetState = count,
                            transitionSpec = { fadeIn(Motion.shortTween()) togetherWith fadeOut(Motion.shortTween()) },
                            label = "resultsCount"
                        ) { c ->
                            Text(
                                text = stringResource(R.string.search_results_count, c),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            when (val state = nameSearchResults) {
                is UiState.Loading -> {
                    items(count = 6, key = { "name_search_skeleton_$it" }) {
                        RecipeCardSkeleton(brush = shimmerBrush)
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "name_search_error") {
                        ErrorView(
                            description = state.message ?: stringResource(R.string.error_connection_desc),
                            onRetry = {
                                if (selectedLetter != null) {
                                    viewModel.selectLetter(selectedLetter)
                                } else {
                                    viewModel.submitNameSearch(nameQuery)
                                }
                            }
                        )
                    }
                }
                is UiState.Success -> {
                    val meals = state.data
                    if (meals.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "name_search_empty") {
                            EmptyView(
                                title = stringResource(R.string.search_empty_title),
                                description = stringResource(R.string.search_empty_desc)
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = meals,
                            key = { _, meal -> "name_meal_${meal.idMeal ?: meal.hashCode()}" }
                        ) { index, meal ->
                            val translatedTitle = translations[meal.strMeal] ?: meal.strMeal
                            val translatedMeal = meal.copy(strMeal = translatedTitle)
                            RecipeCard(
                                meal = translatedMeal,
                                isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                                onMealClick = { onNavigateToDetail(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                modifier = Modifier
                                    .animateItem()
                                    .staggeredEntrance(index = index, enabled = !hasAnimatedEntrance)
                            )
                        }
                    }
                }
                null -> {}
            }
        }
    }
}

/**
 * Konten Pencarian Mode "BAHAN"
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IngredientsModeContent(
    viewModel: MainViewModel,
    ingredientQuery: String,
    autocompleteSuggestions: List<String>,
    selectedIngredients: List<String>,
    isAllIngredientsMode: Boolean,
    ingredientSearchResults: UiState<List<com.example.resepedia.model.MealSummary>>?,
    favoriteIds: Set<String>,
    translations: Map<String, String>,
    shimmerBrush: androidx.compose.ui.graphics.Brush,
    onNavigateToDetail: (String) -> Unit
) {
    var hasAnimatedEntrance by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasAnimatedEntrance = true
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val maxLineSpan = 2

        // A. Autocomplete Suggestions (Tepat di bawah query field)
        if (autocompleteSuggestions.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "suggestions_box") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = stringResource(R.string.search_autocomplete_header),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        autocompleteSuggestions.forEach { suggestion ->
                            val imageUrl = "https://www.themealdb.com/images/ingredients/${suggestion.trim()}-small.png"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.addSelectedIngredient(suggestion) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = suggestion,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // B. Bahan Terpilih (Chips FlowRow) atau Bahan Populer jika belum ada pilihan
        item(span = { GridItemSpan(maxLineSpan) }, key = "ingredients_chips_section") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                if (selectedIngredients.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_popular_ingredients_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        POPULAR_INGREDIENTS.forEach { ing ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.addSelectedIngredient(ing) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = PrimaryOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = ing,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.search_selected_ingredients_title, selectedIngredients.size),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedIngredients.forEach { ing ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = ing,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.removeSelectedIngredient(ing) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // C. Toggle AND / OR (Hanya tampil jika bahan terpilih >= 2)
        if (selectedIngredients.size >= 2) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "and_or_toggle_section") {
                AnimatedVisibility(
                    visible = selectedIngredients.size >= 2,
                    enter = fadeIn(Motion.mediumTween()) + expandVertically(Motion.mediumTween()),
                    exit = fadeOut(Motion.shortTween()) + shrinkVertically(Motion.shortTween())
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = isAllIngredientsMode,
                            onClick = { viewModel.setIsAllIngredientsMode(true) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            label = {
                                Text(
                                    text = stringResource(R.string.search_ingredient_filter_all),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                        SegmentedButton(
                            selected = !isAllIngredientsMode,
                            onClick = { viewModel.setIsAllIngredientsMode(false) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            label = {
                                Text(
                                    text = stringResource(R.string.search_ingredient_filter_any),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }

        // D. Hasil Resep Pencarian Bahan
        if (selectedIngredients.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "ingredient_results_header") {
                val count = (ingredientSearchResults as? UiState.Success)?.data?.size ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hasil Resep Cocok",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (ingredientSearchResults is UiState.Success) {
                        AnimatedContent(
                            targetState = count,
                            transitionSpec = { fadeIn(Motion.shortTween()) togetherWith fadeOut(Motion.shortTween()) },
                            label = "ingredientResultsCount"
                        ) { c ->
                            Text(
                                text = stringResource(R.string.search_results_count, c),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            when (val state = ingredientSearchResults) {
                is UiState.Loading -> {
                    items(count = 6, key = { "ing_search_skeleton_$it" }) {
                        RecipeCardSkeleton(brush = shimmerBrush)
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "ing_search_error") {
                        ErrorView(
                            description = state.message ?: stringResource(R.string.error_connection_desc),
                            onRetry = { viewModel.executeIngredientSearch() }
                        )
                    }
                }
                is UiState.Success -> {
                    val meals = state.data
                    if (meals.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "ing_search_empty") {
                            EmptyView(
                                title = stringResource(R.string.search_empty_ingredient_title),
                                description = stringResource(R.string.search_empty_ingredient_desc)
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = meals,
                            key = { _, meal -> "ing_meal_${meal.idMeal ?: meal.hashCode()}" }
                        ) { index, meal ->
                            val translatedTitle = translations[meal.strMeal] ?: meal.strMeal
                            val translatedMeal = meal.copy(strMeal = translatedTitle)
                            RecipeCard(
                                meal = translatedMeal,
                                isFavorite = favoriteIds.contains(meal.idMeal.orEmpty()),
                                onMealClick = { onNavigateToDetail(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                categoryBadge = "${selectedIngredients.size} bahan",
                                modifier = Modifier
                                    .animateItem()
                                    .staggeredEntrance(index = index, enabled = !hasAnimatedEntrance)
                            )
                        }
                    }
                }
                null -> {}
            }
        }
    }
}
