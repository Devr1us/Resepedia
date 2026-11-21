package com.example.resepedia.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Favorites : Screen("favorites")
    data object Detail : Screen("detail/{mealId}") {
        fun createRoute(mealId: String): String = "detail/$mealId"
    }
}
