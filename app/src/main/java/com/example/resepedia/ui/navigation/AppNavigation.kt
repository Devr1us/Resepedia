package com.example.resepedia.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
import com.example.resepedia.ui.screens.OnboardingScreen
import com.example.resepedia.ui.screens.SearchScreen
import com.example.resepedia.ui.screens.SettingsScreen
import com.example.resepedia.ui.screens.SplashScreen
import com.example.resepedia.ui.theme.Motion
import com.example.resepedia.ui.util.rememberWindowInfo

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val windowInfo = rememberWindowInfo()
    val useNavRail = windowInfo.isLandscape || windowInfo.isExpandedWidth

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tampilkan navigasi hanya pada 3 tab utama: Dashboard, Areas, dan Favorites
    val showBottomBar = currentRoute == Screen.Dashboard.route ||
            currentRoute == Screen.Areas.route ||
            currentRoute == Screen.Favorites.route

    // Tentukan startDestination: Onboarding bila belum selesai, Dashboard bila sudah
    val startDestination = if (viewModel.isOnboardingDone()) {
        Screen.Dashboard.route
    } else {
        Screen.Onboarding.route
    }

    val tabRoutes = listOf(Screen.Dashboard.route, Screen.Areas.route, Screen.Favorites.route)

    if (useNavRail) {
        Row(modifier = modifier.fillMaxSize()) {
            // NavigationRail pada layar Landscape atau Layar Lebar/Tablet
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = Motion.mediumTween()
                ) + fadeIn(animationSpec = Motion.mediumTween()),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = Motion.mediumTween()
                ) + fadeOut(animationSpec = Motion.mediumTween())
            ) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // 1. Beranda
                    val isHomeSelected = currentRoute == Screen.Dashboard.route
                    NavigationRailItem(
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
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.weight(0.2f))

                    // 2. Jelajah Negara
                    val isAreasSelected = currentRoute == Screen.Areas.route
                    NavigationRailItem(
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
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.weight(0.2f))

                    // 3. Favorit
                    val isFavSelected = currentRoute == Screen.Favorites.route
                    NavigationRailItem(
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
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) { innerPadding ->
                ResepediaNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    innerPadding = innerPadding,
                    tabRoutes = tabRoutes,
                    viewModel = viewModel
                )
            }
        }
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = Motion.mediumTween()
                    ) + fadeIn(animationSpec = Motion.mediumTween()),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = Motion.mediumTween()
                    ) + fadeOut(animationSpec = Motion.mediumTween())
                ) {
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

                        // 2. Item Jelajah Negara / Area
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
            ResepediaNavHost(
                navController = navController,
                startDestination = startDestination,
                innerPadding = innerPadding,
                tabRoutes = tabRoutes,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ResepediaNavHost(
    navController: NavHostController,
    startDestination: String,
    innerPadding: PaddingValues,
    tabRoutes: List<String>,
    viewModel: MainViewModel
) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.15f).toInt() },
                    animationSpec = Motion.mediumTween()
                ) + fadeIn(animationSpec = Motion.mediumTween())
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -(it * 0.15f).toInt() },
                    animationSpec = Motion.mediumTween()
                ) + fadeOut(animationSpec = Motion.mediumTween())
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -(it * 0.15f).toInt() },
                    animationSpec = Motion.mediumTween()
                ) + fadeIn(animationSpec = Motion.mediumTween())
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.15f).toInt() },
                    animationSpec = Motion.mediumTween()
                ) + fadeOut(animationSpec = Motion.mediumTween())
            }
        ) {
            // Rute Onboarding (Hanya muncul saat pertama kali dibuka)
            composable(
                route = Screen.Onboarding.route,
                enterTransition = { fadeIn(animationSpec = Motion.mediumTween()) },
                exitTransition = { fadeOut(animationSpec = Motion.mediumTween()) }
            ) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onFinishOnboarding = {
                        viewModel.setOnboardingDone(true)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Rute Splash (Tetap disediakan bila dibutuhkan secara langsung)
            composable(
                route = Screen.Splash.route,
                exitTransition = { fadeOut(animationSpec = Motion.mediumTween()) }
            ) {
                SplashScreen(
                    onSplashFinished = {
                        viewModel.markSplashShown()
                        val targetRoute = if (viewModel.isOnboardingDone()) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Onboarding.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Tab Beranda / Dashboard (Pindah antar-tab memakai fade saja tanpa slide)
            composable(
                route = Screen.Dashboard.route,
                enterTransition = {
                    if (initialState.destination.route in tabRoutes || initialState.destination.route == Screen.Onboarding.route) {
                        fadeIn(animationSpec = Motion.mediumTween())
                    } else {
                        slideInHorizontally(initialOffsetX = { (it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                            fadeIn(animationSpec = Motion.mediumTween())
                    }
                },
                exitTransition = {
                    if (targetState.destination.route in tabRoutes) {
                        fadeOut(animationSpec = Motion.mediumTween())
                    } else {
                        slideOutHorizontally(targetOffsetX = { -(it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                            fadeOut(animationSpec = Motion.mediumTween())
                    }
                }
            ) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(Screen.Detail.createRoute(mealId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            // Tab Jelajah Negara / Areas (Fade antar-tab)
            composable(
                route = Screen.Areas.route,
                enterTransition = {
                    if (initialState.destination.route in tabRoutes) {
                        fadeIn(animationSpec = Motion.mediumTween())
                    } else {
                        slideInHorizontally(initialOffsetX = { (it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                            fadeIn(animationSpec = Motion.mediumTween())
                    }
                },
                exitTransition = {
                    if (targetState.destination.route in tabRoutes) {
                        fadeOut(animationSpec = Motion.mediumTween())
                    } else {
                        slideOutHorizontally(targetOffsetX = { -(it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                            fadeOut(animationSpec = Motion.mediumTween())
                    }
                }
            ) {
                AreaScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(Screen.Detail.createRoute(mealId))
                    }
                )
            }

            // Tab Favorit (Fade antar-tab)
            composable(
                route = Screen.Favorites.route,
                enterTransition = {
                    if (initialState.destination.route in tabRoutes) {
                        fadeIn(animationSpec = Motion.mediumTween())
                    } else {
                        slideInHorizontally(initialOffsetX = { (it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                            fadeIn(animationSpec = Motion.mediumTween())
                    }
                },
                exitTransition = {
                    if (targetState.destination.route in tabRoutes) {
                        fadeOut(animationSpec = Motion.mediumTween())
                    } else {
                        slideOutHorizontally(targetOffsetX = { -(it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                            fadeOut(animationSpec = Motion.mediumTween())
                    }
                }
            ) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { mealId ->
                        navController.navigate(Screen.Detail.createRoute(mealId))
                    }
                )
            }

            // Layar Pencarian Khusus: Masuk dengan slide dari atas + fade
            composable(
                route = Screen.Search.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { -it / 4 },
                        animationSpec = Motion.mediumTween()
                    ) + fadeIn(animationSpec = Motion.mediumTween())
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { -it / 4 },
                        animationSpec = Motion.shortTween()
                    ) + fadeOut(animationSpec = Motion.shortTween())
                },
                popEnterTransition = {
                    fadeIn(animationSpec = Motion.mediumTween())
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { -it / 4 },
                        animationSpec = Motion.shortTween()
                    ) + fadeOut(animationSpec = Motion.shortTween())
                }
            ) {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { mealId, ingredientsParam ->
                        navController.navigate(Screen.Detail.createRoute(mealId, ingredientsParam))
                    }
                )
            }

            // Layar Detail: Masuk dengan slide dari kanan + fade, keluar sebaliknya
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("mealId") { type = NavType.StringType },
                    navArgument("ingredients") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = Motion.mediumTween()) +
                        fadeIn(animationSpec = Motion.mediumTween())
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = Motion.mediumTween()) +
                        fadeOut(animationSpec = Motion.mediumTween())
                },
                popEnterTransition = {
                    fadeIn(animationSpec = Motion.mediumTween())
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = Motion.mediumTween()) +
                        fadeOut(animationSpec = Motion.mediumTween())
                }
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getString("mealId").orEmpty()
                val ingredients = backStackEntry.arguments?.getString("ingredients")
                DetailScreen(
                    mealId = mealId,
                    viewModel = viewModel,
                    ingredientsParam = ingredients,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Layar Pengaturan: Masuk dengan slide dari kanan + fade
            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { (it * 0.2f).toInt() }, animationSpec = Motion.mediumTween()) +
                        fadeIn(animationSpec = Motion.mediumTween())
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -(it * 0.15f).toInt() }, animationSpec = Motion.mediumTween()) +
                        fadeOut(animationSpec = Motion.mediumTween())
                },
                popEnterTransition = {
                    fadeIn(animationSpec = Motion.mediumTween())
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { (it * 0.2f).toInt() }, animationSpec = Motion.mediumTween()) +
                        fadeOut(animationSpec = Motion.mediumTween())
                }
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    }
                )
            }
        }
}
