package xyz.northline.overmapper.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.northline.overmapper.ui.detail.TrailDetailScreen
import xyz.northline.overmapper.ui.map.MapScreen
import xyz.northline.overmapper.ui.settings.SettingsScreen
import xyz.northline.overmapper.ui.trails.TrailsScreen

private sealed class Screen(val route: String, val label: String) {
    object MapTab : Screen("map", "Map")
    object Trails : Screen("trails", "Trails")
    object SettingsTab : Screen("settings", "Settings")
    object Detail : Screen("detail/{trailId}", "Detail") {
        fun route(id: Long) = "detail/$id"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val bottomItems = listOf(Screen.MapTab, Screen.Trails, Screen.SettingsTab)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val showBottomBar = bottomItems.any { it.route == currentDest?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { screen ->
                        val icon = when (screen) {
                            Screen.MapTab -> Icons.Default.Place
                            Screen.Trails -> Icons.AutoMirrored.Filled.List
                            Screen.SettingsTab -> Icons.Default.Settings
                            else -> Icons.Default.Place
                        }
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Screen.MapTab.route,
            modifier = Modifier.padding(padding)) {
            composable(Screen.MapTab.route) {
                MapScreen(onNavigateToDetail = { navController.navigate(Screen.Detail.route(it)) })
            }
            composable(Screen.Trails.route) {
                TrailsScreen(onNavigateToDetail = { navController.navigate(Screen.Detail.route(it)) })
            }
            composable(Screen.SettingsTab.route) { SettingsScreen() }
            composable(
                Screen.Detail.route,
                arguments = listOf(navArgument("trailId") { type = NavType.LongType })
            ) { backStack ->
                val trailId = backStack.arguments!!.getLong("trailId")
                TrailDetailScreen(
                    trailId = trailId,
                    onBack = { navController.popBackStack() },
                    onViewOnMap = {
                        navController.navigate(Screen.MapTab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
