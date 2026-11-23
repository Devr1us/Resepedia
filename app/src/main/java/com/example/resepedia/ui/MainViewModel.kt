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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    override fun onCleared() {
        super.onCleared()
        translationManager.close()
    }
}
