package com.example.resepedia.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Public
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
import com.example.resepedia.ui.screens.AreaScreen
import com.example.resepedia.ui.screens.DashboardScreen
import com.example.resepedia.ui.screens.DetailScreen
import com.example.resepedia.ui.screens.FavoritesScreen
import com.example.resepedia.ui.screens.SettingsScreen
import com.example.resepedia.ui.screens.SplashScreen

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tampilkan bottom navigation bar pada Dashboard, Areas (Negara), dan Favorites
    val showBottomBar = currentRoute == Screen.Dashboard.route ||
            currentRoute == Screen.Areas.route ||
            currentRoute == Screen.Favorites.route

    // Tentukan startDestination: jika splash sudah pernah tampil, langsung ke Dashboard
    val startDestination = if (viewModel.splashShown) Screen.Dashboard.route else Screen.Splash.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    // 1. Item Beranda
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

                    // 2. Item Negara (Jelajah Negara)
                    val isAreasSelected = currentRoute == Screen.Areas.route
                    NavigationBarItem(
                        selected = isAreasSelected,
                        onClick = {
                            if (!isAreasSelected) {
                                navController.navigate(Screen.Areas.route) {
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
                                imageVector = if (isAreasSelected) Icons.Filled.Public else Icons.Outlined.Public,
                                contentDescription = stringResource(R.string.nav_areas_desc)
                            )
                        },
                        label = { Text(text = stringResource(R.string.nav_areas)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 3. Item Favorit
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        viewModel.markSplashShown()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(Screen.Detail.createRoute(mealId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Areas.route) {
                AreaScreen(
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

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
