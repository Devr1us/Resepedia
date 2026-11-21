package com.example.resepedia.network

import com.example.resepedia.model.CategoryResponse
import com.example.resepedia.model.MealDetailResponse
import com.example.resepedia.model.MealListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealListResponse

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealListResponse

    @GET("lookup.php")
    suspend fun getMealDetail(@Query("i") id: String): MealDetailResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealDetailResponse
}
