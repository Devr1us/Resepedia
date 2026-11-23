package com.example.resepedia.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Search : Screen("search")
    data object Areas : Screen("areas")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{mealId}?ingredients={ingredients}") {
        fun createRoute(mealId: String, ingredients: String? = null): String {
            return if (!ingredients.isNullOrBlank()) {
                val encoded = java.net.URLEncoder.encode(ingredients, "UTF-8")
                "detail/$mealId?ingredients=$encoded"
            } else {
                "detail/$mealId"
            }
        }
    }
}
