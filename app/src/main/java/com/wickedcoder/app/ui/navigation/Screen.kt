package com.wickedcoder.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard    : Screen("dashboard",    "Home",        Icons.Default.Home)
    data object Transactions : Screen("transactions", "Transactions",Icons.Default.List)
    data object Analytics    : Screen("analytics",    "Analytics",   Icons.Default.Analytics)
    data object Recurring    : Screen("recurring",    "Recurring",   Icons.Default.Refresh)
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Analytics,
    Screen.Recurring
)
