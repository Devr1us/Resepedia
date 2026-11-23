package com.example.resepedia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.resepedia.data.AppThemeMode
import com.example.resepedia.data.PreferencesManager
import com.example.resepedia.data.RecipeRepository
import com.example.resepedia.data.TranslatedMeal
import com.example.resepedia.data.TranslationManager
import com.example.resepedia.model.CategoryItem
import com.example.resepedia.model.MealDetail
import com.example.resepedia.model.MealSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mode tab untuk Layar Pencarian Khusus
 */
enum class SearchTabMode {
    NAME, INGREDIENTS
}

class MainViewModel(
    application: Application,
    private val repository: RecipeRepository,
    private val preferencesManager: PreferencesManager,
    private val translationManager: TranslationManager = TranslationManager()
) : AndroidViewModel(application) {

    // Constructor eksplisit untuk AndroidViewModelFactory
    constructor(application: Application) : this(
        application = application,
        repository = RecipeRepository(),
        preferencesManager = PreferencesManager(application),
        translationManager = TranslationManager()
    )

    // Status Tema & Favorit ID dari Preferences
    val themeMode: StateFlow<AppThemeMode> = preferencesManager.themeMode
    val favoriteIds: StateFlow<Set<String>> = preferencesManager.favoriteIds

    // Status Shake & Riwayat Pencarian dari Preferences
    val isShakeEnabled: StateFlow<Boolean> = preferencesManager.isShakeEnabled
    val searchHistory: StateFlow<List<String>> = preferencesManager.searchHistory

    fun setShakeEnabled(enabled: Boolean) {
        preferencesManager.setShakeEnabled(enabled)
    }

    fun addSearchHistory(query: String) {
        preferencesManager.addSearchHistory(query)
    }

    fun removeSearchHistory(query: String) {
        preferencesManager.removeSearchHistory(query)
    }

    fun clearSearchHistory() {
        preferencesManager.clearSearchHistory()
    }

    // Status loading tombol Surprise Me (Resep Acak)
    private val _isSurpriseLoading = MutableStateFlow(false)
    val isSurpriseLoading: StateFlow<Boolean> = _isSurpriseLoading.asStateFlow()

    /**
     * Memanggil API random.php untuk fitur Surprise Me / Shake.
     * Dilengkapi guard untuk mencegah double-trigger / navigasi ganda saat request berjalan.
     */
    fun fetchRandomMeal(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isSurpriseLoading.value) return
        _isSurpriseLoading.value = true

        viewModelScope.launch {
            try {
                val meal = repository.getRandomMeal()
                _isSurpriseLoading.value = false
                val mealId = meal?.idMeal
                if (!mealId.isNullOrBlank()) {
                    onSuccess(mealId)
                } else {
                    onError("Resep acak tidak ditemukan")
                }
            } catch (e: Exception) {
                _isSurpriseLoading.value = false
                onError(e.localizedMessage ?: "Gagal memuat resep acak")
            }
        }
    }

    // Flag untuk memastikan Splash Screen hanya tampil satu kali per proses aplikasi
    var splashShown: Boolean = false
        private set

    fun markSplashShown() {
        splashShown = true
    }

    // State Hero Recipe (Resep Hari Ini)
    private val _heroRecipeState = MutableStateFlow<UiState<MealDetail>>(UiState.Loading)
    val heroRecipeState: StateFlow<UiState<MealDetail>> = _heroRecipeState.asStateFlow()

    // State Kategori
    private val _categoriesState = MutableStateFlow<UiState<List<CategoryItem>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategoryItem>>> = _categoriesState.asStateFlow()

    // Kategori Terpilih (Default: Chicken) - Nilai asli selalu dipakai untuk API filter.php
    private val _selectedCategory = MutableStateFlow("Chicken")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // State Resep Berdasarkan Kategori
    private val _recipesByCategoryState = MutableStateFlow<UiState<List<MealSummary>>>(UiState.Loading)
    val recipesByCategoryState: StateFlow<UiState<List<MealSummary>>> = _recipesByCategoryState.asStateFlow()

    // State Pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultsState = MutableStateFlow<UiState<List<MealSummary>>?>(null)
    val searchResultsState: StateFlow<UiState<List<MealSummary>>?> = _searchResultsState.asStateFlow()

    // ==========================================
    // STATE SEARCH SCREEN (TAB, NAMA, & BAHAN)
    // ==========================================
    private val _searchTabMode = MutableStateFlow(SearchTabMode.NAME)
    val searchTabMode: StateFlow<SearchTabMode> = _searchTabMode.asStateFlow()

    fun setSearchTabMode(mode: SearchTabMode) {
        _searchTabMode.value = mode
    }

    // --- Mode Nama ---
    private val _nameSearchQuery = MutableStateFlow("")
    val nameSearchQuery: StateFlow<String> = _nameSearchQuery.asStateFlow()

    private val _selectedLetter = MutableStateFlow<Char?>(null)
    val selectedLetter: StateFlow<Char?> = _selectedLetter.asStateFlow()

    private val _nameSearchResults = MutableStateFlow<UiState<List<MealSummary>>?>(null)
    val nameSearchResults: StateFlow<UiState<List<MealSummary>>?> = _nameSearchResults.asStateFlow()

    private var nameSearchJob: Job? = null

    // --- Mode Bahan ---
    private val _ingredientQuery = MutableStateFlow("")
    val ingredientQuery: StateFlow<String> = _ingredientQuery.asStateFlow()

    private val _allIngredients = MutableStateFlow<List<String>>(emptyList())
    val allIngredients: StateFlow<List<String>> = _allIngredients.asStateFlow()

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteSuggestions.asStateFlow()

    private val _selectedIngredients = MutableStateFlow<List<String>>(emptyList())
    val selectedIngredients: StateFlow<List<String>> = _selectedIngredients.asStateFlow()

    private val _isAllIngredientsMode = MutableStateFlow(true) // default: semua bahan (AND)
    val isAllIngredientsMode: StateFlow<Boolean> = _isAllIngredientsMode.asStateFlow()

    private val _ingredientSearchResults = MutableStateFlow<UiState<List<MealSummary>>?>(null)
    val ingredientSearchResults: StateFlow<UiState<List<MealSummary>>?> = _ingredientSearchResults.asStateFlow()

    private val _ingredientLimitMessage = MutableStateFlow(false)
    val ingredientLimitMessage: StateFlow<Boolean> = _ingredientLimitMessage.asStateFlow()

    private var ingredientSearchJob: Job? = null

    // State Resep Favorit
    private val _favoritesState = MutableStateFlow<UiState<List<MealSummary>>>(UiState.Success(emptyList()))
    val favoritesState: StateFlow<UiState<List<MealSummary>>> = _favoritesState.asStateFlow()

    // State Jelajah Negara / Area
    private val _areasState = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val areasState: StateFlow<UiState<List<String>>> = _areasState.asStateFlow()

    private val _selectedArea = MutableStateFlow<String?>(null)
    val selectedArea: StateFlow<String?> = _selectedArea.asStateFlow()

    private val _mealsByAreaState = MutableStateFlow<UiState<List<MealSummary>>>(UiState.Loading)
    val mealsByAreaState: StateFlow<UiState<List<MealSummary>>> = _mealsByAreaState.asStateFlow()

    // State Detail Resep yang sedang dibuka
    private val _detailMealState = MutableStateFlow<UiState<MealDetail>>(UiState.Loading)
    val detailMealState: StateFlow<UiState<MealDetail>> = _detailMealState.asStateFlow()

    // State Terjemahan Teks (teks asli -> teks terjemahan) untuk seluruh halaman
    private val _translations = MutableStateFlow<Map<String, String>>(emptyMap())
    val translations: StateFlow<Map<String, String>> = _translations.asStateFlow()

    // State Terjemahan Khusus Detail Resep
    private val _translatedMeal = MutableStateFlow<TranslatedMeal?>(null)
    val translatedMeal: StateFlow<TranslatedMeal?> = _translatedMeal.asStateFlow()

    // Indikator proses terjemahan detail berjalan
    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private var searchJob: Job? = null
    private var translateDetailJob: Job? = null

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        loadHeroRecipe()
        loadCategories()
        loadMealsByCategory(_selectedCategory.value)
    }

    fun loadHeroRecipe() {
        viewModelScope.launch {
            _heroRecipeState.value = UiState.Loading
            try {
                val meal = repository.getRandomMeal()
                if (meal != null) {
                    _heroRecipeState.value = UiState.Success(meal)
                    // Terjemahkan judul, kategori, dan area resep hari ini
                    translateTexts(listOf(meal.strMeal, meal.strCategory, meal.strArea))
                } else {
                    _heroRecipeState.value = UiState.Error("Resep hari ini tidak ditemukan")
                }
            } catch (e: Exception) {
                _heroRecipeState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            try {
                val categories = repository.getCategories()
                _categoriesState.value = UiState.Success(categories)
                // Terjemahkan nama semua kategori
                translateTexts(categories.map { it.strCategory })
            } catch (e: Exception) {
                _categoriesState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    fun selectCategory(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        loadMealsByCategory(category)
    }

    fun loadMealsByCategory(category: String) {
        viewModelScope.launch {
            _recipesByCategoryState.value = UiState.Loading
            try {
                val meals = repository.getMealsByCategory(category)
                _recipesByCategoryState.value = UiState.Success(meals)
                // Terjemahkan judul kartu resep per kategori
                translateTexts(meals.map { it.strMeal })
            } catch (e: Exception) {
                _recipesByCategoryState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResultsState.value = null
            return
        }

        searchJob = viewModelScope.launch {
            _searchResultsState.value = UiState.Loading
            try {
                val results = repository.searchMeals(query.trim())
                _searchResultsState.value = UiState.Success(results)
                // Terjemahkan judul kartu hasil pencarian
                translateTexts(results.map { it.strMeal })
            } catch (e: Exception) {
                _searchResultsState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResultsState.value = null
    }

    // ==========================================
    // LOGIKA PENCARIAN MODE NAMA
    // ==========================================
    fun onNameQueryChange(query: String) {
        _nameSearchQuery.value = query
        _selectedLetter.value = null
        nameSearchJob?.cancel()

        if (query.isBlank()) {
            _nameSearchResults.value = null
            return
        }

        nameSearchJob = viewModelScope.launch {
            delay(400) // Debounce 400ms
            performNameSearch(query.trim())
        }
    }

    fun submitNameSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        _nameSearchQuery.value = trimmed
        _selectedLetter.value = null
        addSearchHistory(trimmed)
        nameSearchJob?.cancel()
        nameSearchJob = viewModelScope.launch {
            performNameSearch(trimmed)
        }
    }

    private suspend fun performNameSearch(query: String) {
        _nameSearchResults.value = UiState.Loading
        try {
            val results = repository.searchMeals(query)
            _nameSearchResults.value = UiState.Success(results)
            translateTexts(results.map { it.strMeal })
        } catch (e: Exception) {
            if (e !is CancellationException) {
                _nameSearchResults.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    fun selectLetter(letter: Char) {
        if (_selectedLetter.value == letter) {
            _selectedLetter.value = null
            _nameSearchResults.value = null
            return
        }
        _selectedLetter.value = letter
        _nameSearchQuery.value = ""
        nameSearchJob?.cancel()
        nameSearchJob = viewModelScope.launch {
            _nameSearchResults.value = UiState.Loading
            try {
                val results = repository.searchMealsByFirstLetter(letter.toString())
                _nameSearchResults.value = UiState.Success(results)
                translateTexts(results.map { it.strMeal })
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _nameSearchResults.value = UiState.Error(e.localizedMessage)
                }
            }
        }
    }

    fun clearNameSearch() {
        _nameSearchQuery.value = ""
        _selectedLetter.value = null
        nameSearchJob?.cancel()
        _nameSearchResults.value = null
    }

    // ==========================================
    // LOGIKA PENCARIAN MODE BAHAN
    // ==========================================
    fun loadAllIngredients() {
        if (_allIngredients.value.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val list = repository.getIngredientList()
                _allIngredients.value = list
            } catch (e: Exception) {
                // Abaikan bila offline / gagal
            }
        }
    }

    fun onIngredientQueryChange(query: String) {
        _ingredientQuery.value = query
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _autocompleteSuggestions.value = emptyList()
        } else {
            val filtered = _allIngredients.value
                .filter { it.contains(trimmed, ignoreCase = true) }
                .take(8)
            _autocompleteSuggestions.value = filtered
        }
    }

    fun addSelectedIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isBlank()) return

        val current = _selectedIngredients.value.toMutableList()
        if (current.any { it.equals(trimmed, ignoreCase = true) }) {
            _ingredientQuery.value = ""
            _autocompleteSuggestions.value = emptyList()
            return
        }

        if (current.size >= 5) {
            _ingredientLimitMessage.value = true
            return
        }

        current.add(trimmed)
        _selectedIngredients.value = current
        _ingredientQuery.value = ""
        _autocompleteSuggestions.value = emptyList()
        _ingredientLimitMessage.value = false
        executeIngredientSearch()
    }

    fun removeSelectedIngredient(ingredient: String) {
        val current = _selectedIngredients.value.toMutableList()
        current.removeAll { it.equals(ingredient, ignoreCase = true) }
        _selectedIngredients.value = current
        _ingredientLimitMessage.value = false
        executeIngredientSearch()
    }

    fun setIsAllIngredientsMode(isAll: Boolean) {
        if (_isAllIngredientsMode.value == isAll) return
        _isAllIngredientsMode.value = isAll
        executeIngredientSearch()
    }

    fun resetIngredientLimitMessage() {
        _ingredientLimitMessage.value = false
    }

    fun clearIngredientSearch() {
        _ingredientQuery.value = ""
        _autocompleteSuggestions.value = emptyList()
    }

    fun executeIngredientSearch() {
        val selected = _selectedIngredients.value
        if (selected.isEmpty()) {
            _ingredientSearchResults.value = null
            return
        }

        ingredientSearchJob?.cancel()
        ingredientSearchJob = viewModelScope.launch {
            _ingredientSearchResults.value = UiState.Loading
            try {
                val results = repository.searchMealsByIngredients(
                    ingredients = selected,
                    isAllIngredients = _isAllIngredientsMode.value
                )
                _ingredientSearchResults.value = UiState.Success(results)
                translateTexts(results.map { it.strMeal })
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _ingredientSearchResults.value = UiState.Error(e.localizedMessage)
                }
            }
        }
    }

    fun loadMealDetail(mealId: String) {
        translateDetailJob?.cancel()
        _translatedMeal.value = null
        _isTranslating.value = false

        viewModelScope.launch {
            _detailMealState.value = UiState.Loading
            try {
                val meal = repository.getMealDetail(mealId)
                if (meal != null) {
                    _detailMealState.value = UiState.Success(meal)
                    // Terjemahkan detail resep bila bahasa aktif membutuhkan terjemahan
                    val currentLang = getLanguage()
                    if (TranslationManager.targetFor(currentLang) != null) {
                        startTranslateMealDetail(meal, currentLang)
                    }
                } else {
                    _detailMealState.value = UiState.Error("Resep tidak ditemukan")
                }
            } catch (e: Exception) {
                _detailMealState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    private fun startTranslateMealDetail(meal: MealDetail, langCode: String) {
        translateDetailJob?.cancel()
        translateDetailJob = viewModelScope.launch {
            _isTranslating.value = true
            try {
                val translated = translationManager.translateMeal(meal, langCode)
                if (getLanguage() == langCode) {
                    _translatedMeal.value = translated
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Diamkan agar UI menampilkan data asli
            } finally {
                _isTranslating.value = false
            }
        }
    }

    fun toggleFavorite(mealId: String) {
        preferencesManager.toggleFavorite(mealId)
        loadFavoriteMeals()
    }

    fun loadFavoriteMeals() {
        viewModelScope.launch {
            _favoritesState.value = UiState.Loading
            try {
                val ids = preferencesManager.favoriteIds.value
                if (ids.isEmpty()) {
                    _favoritesState.value = UiState.Success(emptyList())
                } else {
                    val meals = repository.getFavoriteMeals(ids)
                    _favoritesState.value = UiState.Success(meals)
                    // Terjemahkan judul kartu favorit
                    translateTexts(meals.map { it.strMeal })
                }
            } catch (e: Exception) {
                _favoritesState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    /**
     * Memuat daftar negara/area sekali saja. Tidak dimuat ulang saat berpindah tab
     * kecuali jika forceRefresh = true (misal saat tap retry pada ErrorView).
     */
    fun loadAreas(forceRefresh: Boolean = false) {
        if (!forceRefresh && _areasState.value is UiState.Success) return
        viewModelScope.launch {
            _areasState.value = UiState.Loading
            try {
                val areas = repository.getAreas()
                _areasState.value = UiState.Success(areas)
            } catch (e: Exception) {
                _areasState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    /**
     * Memilih negara dan memuat daftar resep dari negara tersebut.
     */
    fun selectArea(area: String) {
        _selectedArea.value = area
        loadMealsByArea(area)
    }

    /**
     * Menghapus negara terpilih untuk kembali ke daftar negara (Mode A).
     */
    fun clearSelectedArea() {
        _selectedArea.value = null
    }

    /**
     * Memuat daftar resep berdasarkan negara terpilih.
     */
    fun loadMealsByArea(area: String) {
        viewModelScope.launch {
            _mealsByAreaState.value = UiState.Loading
            try {
                val meals = repository.getMealsByArea(area)
                _mealsByAreaState.value = UiState.Success(meals)
                // Terjemahkan nama resep jika bahasa Indonesia aktif
                translateTexts(meals.map { it.strMeal })
            } catch (e: Exception) {
                _mealsByAreaState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    /**
     * Menerjemahkan kumpulan teks jika bahasa bukan Inggris dan teks belum ada di cache terjemahan.
     */
    private fun translateTexts(texts: Collection<String?>) {
        val currentLang = getLanguage()
        if (TranslationManager.targetFor(currentLang) == null) return

        val currentMap = _translations.value
        val missingTexts = texts.filterNotNull()
            .map { it.trim() }
            .filter { it.isNotBlank() && !currentMap.containsKey(it) }
            .distinct()

        if (missingTexts.isEmpty()) return

        viewModelScope.launch {
            try {
                val result = translationManager.translateTexts(missingTexts, currentLang)
                // Hanya terapkan jika bahasa belum berganti selama proses terjemahan
                if (getLanguage() == currentLang && result.isNotEmpty()) {
                    _translations.update { it + result }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Diamkan jika offline atau gagal
            }
        }
    }

    /**
     * Menerjemahkan ulang seluruh konten yang sedang tampil saat bahasa diganti
     */
    private fun retranslateCurrentContent() {
        val currentLang = getLanguage()
        if (TranslationManager.targetFor(currentLang) == null) return

        val textsToTranslate = mutableListOf<String?>()

        // 1. Resep Hari Ini
        val hero = _heroRecipeState.value
        if (hero is UiState.Success) {
            textsToTranslate.add(hero.data.strMeal)
            textsToTranslate.add(hero.data.strCategory)
            textsToTranslate.add(hero.data.strArea)
        }

        // 2. Kategori
        val categories = _categoriesState.value
        if (categories is UiState.Success) {
            textsToTranslate.addAll(categories.data.map { it.strCategory })
        }

        // 3. Resep per Kategori
        val mealsByCategory = _recipesByCategoryState.value
        if (mealsByCategory is UiState.Success) {
            textsToTranslate.addAll(mealsByCategory.data.map { it.strMeal })
        }

        // 4. Hasil Pencarian
        val searchResults = _searchResultsState.value
        if (searchResults is UiState.Success) {
            textsToTranslate.addAll(searchResults.data.map { it.strMeal })
        }

        // 4b. Hasil Pencarian Mode Nama
        val nameResults = _nameSearchResults.value
        if (nameResults is UiState.Success) {
            textsToTranslate.addAll(nameResults.data.map { it.strMeal })
        }

        // 4c. Hasil Pencarian Mode Bahan
        val ingResults = _ingredientSearchResults.value
        if (ingResults is UiState.Success) {
            textsToTranslate.addAll(ingResults.data.map { it.strMeal })
        }

        // 5. Favorit
        val favorites = _favoritesState.value
        if (favorites is UiState.Success) {
            textsToTranslate.addAll(favorites.data.map { it.strMeal })
        }

        // 6. Resep per Negara
        val mealsByArea = _mealsByAreaState.value
        if (mealsByArea is UiState.Success) {
            textsToTranslate.addAll(mealsByArea.data.map { it.strMeal })
        }

        translateTexts(textsToTranslate)

        // 6. Detail Resep (jika sedang dibuka)
        val detail = _detailMealState.value
        if (detail is UiState.Success) {
            startTranslateMealDetail(detail.data, currentLang)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    fun getLanguage(): String {
        return preferencesManager.getLanguage()
    }

    fun setLanguage(langCode: String) {
        preferencesManager.setLanguage(langCode)
        _translations.value = emptyMap()
        _translatedMeal.value = null
        retranslateCurrentContent()
    }

    fun isOnboardingDone(): Boolean {
        return preferencesManager.isOnboardingDone()
    }

    fun setOnboardingDone(done: Boolean) {
        preferencesManager.setOnboardingDone(done)
    }

    override fun onCleared() {
        super.onCleared()
        translationManager.close()
    }
}
