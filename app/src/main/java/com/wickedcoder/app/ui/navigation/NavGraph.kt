package com.wickedcoder.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wickedcoder.app.ui.analytics.AnalyticsScreen
import com.wickedcoder.app.ui.dashboard.DashboardScreen
import com.wickedcoder.app.ui.recurring.RecurringScreen
import com.wickedcoder.app.ui.transactions.TransactionListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendSenseNavGraph() {
    val navController   = rememberNavController()
    val drawerState     = rememberDrawerState(DrawerValue.Closed)
    val scope           = rememberCoroutineScope()
    val backStackEntry  by navController.currentBackStackEntryAsState()
    val currentRoute    = backStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "SpendSense",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(24.dp)
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    icon  = { Icon(Icons.Default.MoneyOff, null) },
                    label = { Text("Monthly Budget") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon  = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon  = { Icon(Icons.Default.Share, null) },
                    label = { Text("Export Data") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon  = { Icon(Icons.Default.Info, null) },
                    label = { Text("About & Help") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SpendSense") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        val selected = backStackEntry?.destination
                            ?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon     = { Icon(screen.icon, screen.label) },
                            label    = { Text(screen.label) },
                            selected = selected,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Screen.Dashboard.route,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route)    { DashboardScreen() }
                composable(Screen.Transactions.route) { TransactionListScreen() }
                composable(Screen.Analytics.route)    { AnalyticsScreen() }
                composable(Screen.Recurring.route)    { RecurringScreen() }
            }
        }
    }
}
