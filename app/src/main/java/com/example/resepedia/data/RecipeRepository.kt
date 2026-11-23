package com.example.resepedia.data

import com.example.resepedia.model.CategoryItem
import com.example.resepedia.model.MealDetail
import com.example.resepedia.model.MealSummary
import com.example.resepedia.network.ApiClient
import com.example.resepedia.network.MealApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    /**
     * Mencari resep berdasarkan huruf awal (search.php?f=).
     */
    suspend fun searchMealsByFirstLetter(letter: String): List<MealSummary> = withContext(Dispatchers.IO) {
        apiService.searchMealsByFirstLetter(letter).meals ?: emptyList()
    }

    /**
     * Mengambil seluruh daftar nama bahan dari TheMealDB (list.php?i=list).
     * Disortir secara alfabetis dan dibersihkan dari spasi berlebih.
     */
    suspend fun getIngredientList(): List<String> = withContext(Dispatchers.IO) {
        apiService.getIngredientList().meals
            ?.mapNotNull { it.strIngredient?.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    /**
     * Mengambil daftar resep berdasarkan satu bahan (filter.php?i=).
     * Spasi otomatis diganti dengan underscore sesuai aturan TheMealDB.
     */
    suspend fun getMealsByIngredient(ingredient: String): List<MealSummary> = withContext(Dispatchers.IO) {
        val formatted = ingredient.trim().replace(" ", "_")
        apiService.getMealsByIngredient(formatted).meals ?: emptyList()
    }

    /**
     * Mencari resep berdasarkan multi-bahan secara paralel di Dispatchers.IO.
     * Menggabungkan hasil berdasarkan idMeal:
     * - isAllIngredients = true (Mode AND / Irisan): Hanya resep yang ada di seluruh hasil bahan.
     * - isAllIngredients = false (Mode OR / Gabungan): Semua resep tanpa duplikat idMeal.
     */
    suspend fun searchMealsByIngredients(
        ingredients: List<String>,
        isAllIngredients: Boolean
    ): List<MealSummary> = withContext(Dispatchers.IO) {
        if (ingredients.isEmpty()) return@withContext emptyList()

        // Panggil filter.php?i= untuk setiap bahan secara paralel
        val deferredList = ingredients.map { ing ->
            async {
                try {
                    getMealsByIngredient(ing)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        val resultsPerIngredient = deferredList.awaitAll()

        if (isAllIngredients) {
            // Mode Irisan (AND): Setiap resep harus ada di setiap list bahan
            if (resultsPerIngredient.any { it.isEmpty() }) return@withContext emptyList()

            var intersection = resultsPerIngredient.first().associateBy { it.idMeal.orEmpty() }
            for (i in 1 until resultsPerIngredient.size) {
                val currentSet = resultsPerIngredient[i].mapNotNull { it.idMeal }.toSet()
                intersection = intersection.filterKeys { currentSet.contains(it) }
            }
            intersection.values.toList()
        } else {
            // Mode Gabungan (OR): Gabungkan semua tanpa duplikasi idMeal
            val unionMap = mutableMapOf<String, MealSummary>()
            for (list in resultsPerIngredient) {
                for (meal in list) {
                    val id = meal.idMeal
                    if (!id.isNullOrBlank() && !unionMap.containsKey(id)) {
                        unionMap[id] = meal
                    }
                }
            }
            unionMap.values.toList()
        }
    }
}
