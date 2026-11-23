package com.example.resepedia.data

import com.example.resepedia.model.CategoryItem
import com.example.resepedia.model.MealDetail
import com.example.resepedia.model.MealSummary
import com.example.resepedia.network.ApiClient
import com.example.resepedia.network.MealApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val apiService: MealApiService = ApiClient.apiService
) {

    suspend fun getCategories(): List<CategoryItem> = withContext(Dispatchers.IO) {
        apiService.getCategories().categories ?: emptyList()
    }

    suspend fun getMealsByCategory(category: String): List<MealSummary> = withContext(Dispatchers.IO) {
        apiService.getMealsByCategory(category).meals ?: emptyList()
    }

    suspend fun searchMeals(query: String): List<MealSummary> = withContext(Dispatchers.IO) {
        apiService.searchMeals(query).meals ?: emptyList()
    }

    suspend fun getMealDetail(id: String): MealDetail? = withContext(Dispatchers.IO) {
        apiService.getMealDetail(id).meals?.firstOrNull()
    }

    suspend fun getRandomMeal(): MealDetail? = withContext(Dispatchers.IO) {
        apiService.getRandomMeal().meals?.firstOrNull()
    }

    suspend fun getFavoriteMeals(favoriteIds: Set<String>): List<MealSummary> = withContext(Dispatchers.IO) {
        favoriteIds.mapNotNull { id ->
            try {
                apiService.getMealDetail(id).meals?.firstOrNull()?.toSummary()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Mengambil daftar negara/area dari TheMealDB (list.php?a=list).
     * Memfilter nilai null, kosong, dan "Unknown", lalu mengurutkannya secara alfabetis (A-Z).
     */
    suspend fun getAreas(): List<String> = withContext(Dispatchers.IO) {
        apiService.getAreas().meals
            ?.mapNotNull { it.strArea?.trim() }
            ?.filter { it.isNotBlank() && !it.equals("Unknown", ignoreCase = true) }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    /**
     * Mengambil daftar resep berdasarkan asal negara/area (filter.php?a=).
     */
    suspend fun getMealsByArea(area: String): List<MealSummary> = withContext(Dispatchers.IO) {
        apiService.getMealsByArea(area).meals ?: emptyList()
    }
}
