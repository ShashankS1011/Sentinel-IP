package com.example.sentinel_ip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sentinel_ip.ui.navigation.Screen
import com.example.sentinel_ip.ui.navigation.SentinelBottomNavBar
import com.example.sentinel_ip.ui.navigation.SentinelTopAppBar
import com.example.sentinel_ip.ui.screens.BlacklistScreen
import com.example.sentinel_ip.ui.screens.BulkScannerScreen
import com.example.sentinel_ip.ui.screens.CheckScreen
import com.example.sentinel_ip.ui.screens.CompareScreen
import com.example.sentinel_ip.ui.screens.HistoryScreen
import com.example.sentinel_ip.ui.screens.ReportScreen
import com.example.sentinel_ip.ui.screens.SettingsDialog
import com.example.sentinel_ip.ui.screens.SplashScreen
import com.example.sentinel_ip.ui.screens.SubnetScreen
import com.example.sentinel_ip.ui.theme.SentinelIPTheme
import com.example.sentinel_ip.ui.viewmodel.SentinelViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SentinelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SentinelIPTheme {
                SentinelApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SentinelApp(viewModel: SentinelViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Splash.route

    var showSettingsDialog by remember { mutableStateOf(false) }
    var reportPrefilledIp by remember { mutableStateOf("") }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentRoute != Screen.Splash.route) {
                SentinelTopAppBar(
                    onOpenSettings = { showSettingsDialog = true }
                )
            }
        },
        bottomBar = {
            if (currentRoute != Screen.Splash.route) {
                SentinelBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Check.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Check.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Check.route) {
                CheckScreen(
                    viewModel = viewModel,
                    onNavigateToReport = { ip ->
                        reportPrefilledIp = ip
                        navController.navigate(Screen.Report.route)
                    },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            composable(Screen.Compare.route) {
                CompareScreen(
                    viewModel = viewModel,
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            composable(Screen.Blacklist.route) {
                BlacklistScreen(
                    viewModel = viewModel,
                    onSelectIpForAudit = { ip ->
                        viewModel.auditIp(ip)
                        navController.navigate(Screen.Check.route)
                    },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            composable(Screen.Subnet.route) {
                SubnetScreen(
                    viewModel = viewModel,
                    onSelectIpForAudit = { ip ->
                        viewModel.auditIp(ip)
                        navController.navigate(Screen.Check.route)
                    },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            composable(Screen.Bulk.route) {
                BulkScannerScreen(
                    viewModel = viewModel,
                    onSelectIpForAudit = { ip ->
                        viewModel.auditIp(ip)
                        navController.navigate(Screen.Check.route)
                    },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            composable(Screen.Report.route) {
                ReportScreen(
                    viewModel = viewModel,
                    prefilledIp = reportPrefilledIp,
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onSelectIpForAudit = { ip ->
                        if (ip.contains("/")) {
                            viewModel.scanSubnet(ip)
                            navController.navigate(Screen.Subnet.route)
                        } else {
                            viewModel.auditIp(ip)
                            navController.navigate(Screen.Check.route)
                        }
                    }
                )
            }
        }
    }
}
