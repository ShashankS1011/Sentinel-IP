package com.example.sentinel_ip.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.Security)
    object Check : Screen("check", "Audit IP", Icons.Default.Security)
    object Compare : Screen("compare", "Compare", Icons.AutoMirrored.Filled.CompareArrows)
    object Blacklist : Screen("blacklist", "Radar", Icons.Default.Radar)
    object Subnet : Screen("subnet", "Subnet", Icons.Default.Hub)
    object Bulk : Screen("bulk", "Bulk Logs", Icons.AutoMirrored.Filled.ListAlt)
    object Report : Screen("report", "Report", Icons.Default.BugReport)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Check, Compare, Blacklist, Subnet, Bulk, Report, History)
    }
}
