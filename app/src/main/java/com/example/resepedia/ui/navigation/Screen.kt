package com.example.resepedia.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Areas : Screen("areas")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{mealId}") {
        fun createRoute(mealId: String): String = "detail/$mealId"
    }
}
