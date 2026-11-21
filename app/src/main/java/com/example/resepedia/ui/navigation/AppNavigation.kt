package com.example.resepedia.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.resepedia.R
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.screens.DashboardScreen
import com.example.resepedia.ui.screens.DetailScreen
import com.example.resepedia.ui.screens.FavoritesScreen

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tampilkan bottom navigation bar hanya pada dashboard dan favorites
    val showBottomBar = currentRoute == Screen.Dashboard.route || currentRoute == Screen.Favorites.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    // Item Beranda
                    val isHomeSelected = currentRoute == Screen.Dashboard.route
                    NavigationBarItem(
                        selected = isHomeSelected,
                        onClick = {
                            if (!isHomeSelected) {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = stringResource(R.string.nav_home_desc)
                            )
                        },
                        label = { Text(text = stringResource(R.string.nav_home)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Item Favorit
                    val isFavSelected = currentRoute == Screen.Favorites.route
                    NavigationBarItem(
                        selected = isFavSelected,
                        onClick = {
                            if (!isFavSelected) {
                                navController.navigate(Screen.Favorites.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isFavSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.nav_favorite_desc)
                            )
                        },
                        label = { Text(text = stringResource(R.string.nav_favorite)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(Screen.Detail.createRoute(mealId))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(Screen.Detail.createRoute(mealId))
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("mealId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getString("mealId").orEmpty()
                DetailScreen(
                    mealId = mealId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
