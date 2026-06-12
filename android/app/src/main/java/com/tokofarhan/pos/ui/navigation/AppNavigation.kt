package com.tokofarhan.pos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tokofarhan.pos.ui.screens.history.HistoryScreen
import com.tokofarhan.pos.ui.screens.pos.PosScreen
import com.tokofarhan.pos.ui.screens.report.ReportScreen
import com.tokofarhan.pos.ui.screens.stock.StockScreen
import com.tokofarhan.pos.ui.screens.success.SuccessScreen
import com.tokofarhan.pos.ui.theme.Primary

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Pos : Screen("pos", "Transaksi", Icons.Filled.PointOfSale)
    data object History : Screen("history", "Riwayat", Icons.Filled.History)
    data object Stock : Screen("stock", "Stok", Icons.Filled.Inventory2)
    data object Report : Screen("report", "Laporan", Icons.Filled.Assessment)
}

val bottomNavItems = listOf(Screen.Pos, Screen.History, Screen.Stock, Screen.Report)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route?.startsWith("success") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = {
                                Text(
                                    screen.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Pos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Pos.route) { PosScreen(navController) }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Stock.route) { StockScreen() }
            composable(Screen.Report.route) { ReportScreen() }
            composable("success/{transactionId}") { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                SuccessScreen(
                    transactionId = transactionId,
                    onNewTransaction = {
                        navController.navigate(Screen.Pos.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
