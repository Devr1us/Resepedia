package com.example.resepedia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.resepedia.data.AppThemeMode
import com.example.resepedia.data.PreferencesManager
import com.example.resepedia.data.RecipeRepository
import com.example.resepedia.model.CategoryItem
import com.example.resepedia.model.MealDetail
import com.example.resepedia.model.MealSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val repository: RecipeRepository = RecipeRepository(),
    private val preferencesManager: PreferencesManager = PreferencesManager(application)
) : AndroidViewModel(application) {

    // Status Tema & Favorit ID dari Preferences
    val themeMode: StateFlow<AppThemeMode> = preferencesManager.themeMode
    val favoriteIds: StateFlow<Set<String>> = preferencesManager.favoriteIds

    // State Hero Recipe (Resep Hari Ini)
    private val _heroRecipeState = MutableStateFlow<UiState<MealDetail>>(UiState.Loading)
    val heroRecipeState: StateFlow<UiState<MealDetail>> = _heroRecipeState.asStateFlow()

    // State Kategori
    private val _categoriesState = MutableStateFlow<UiState<List<CategoryItem>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategoryItem>>> = _categoriesState.asStateFlow()

    // Kategori Terpilih (Default: Chicken)
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

    // State Detail Resep yang sedang dibuka
    private val _detailMealState = MutableStateFlow<UiState<MealDetail>>(UiState.Loading)
    val detailMealState: StateFlow<UiState<MealDetail>> = _detailMealState.asStateFlow()

    private var searchJob: Job? = null

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
        viewModelScope.launch {
            _detailMealState.value = UiState.Loading
            try {
                val meal = repository.getMealDetail(mealId)
                if (meal != null) {
                    _detailMealState.value = UiState.Success(meal)
                } else {
                    _detailMealState.value = UiState.Error("Resep tidak ditemukan")
                }
            } catch (e: Exception) {
                _detailMealState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    fun toggleFavorite(mealId: String) {
        preferencesManager.toggleFavorite(mealId)
        // Refresh daftar favorit jika sedang aktif
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
                }
            } catch (e: Exception) {
                _favoritesState.value = UiState.Error(e.localizedMessage)
            }
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
    }
}
