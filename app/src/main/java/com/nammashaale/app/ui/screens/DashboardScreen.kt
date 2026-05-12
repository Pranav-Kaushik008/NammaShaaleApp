package com.nammashaale.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammashaale.app.ui.components.*
import com.nammashaale.app.ui.theme.AmberCheck
import com.nammashaale.app.ui.theme.GreenWorking
import com.nammashaale.app.ui.theme.RedRepair
import com.nammashaale.app.viewmodel.AssetViewModel
import com.nammashaale.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    assetVm: AssetViewModel = viewModel(),
    authVm: AuthViewModel = viewModel()
) {
    val assets    by assetVm.assets.collectAsState()
    val working   by assetVm.workingCount.collectAsState()
    val check     by assetVm.checkCount.collectAsState()
    val repair    by assetVm.repairCount.collectAsState()
    val total     by assetVm.totalCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Namma-Shaale", fontWeight = FontWeight.Bold)
                        Text(
                            "GPS Higher Primary School",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("health_check") }) {
                        Icon(Icons.Default.Checklist, contentDescription = "Health Check")
                    }
                    IconButton(onClick = {
                        authVm.logout()
                        navController.navigate("login?registered=false") { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add_asset") },
                icon    = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text    = { Text("Add Asset") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Total badge
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Total Assets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                            Text("$total items registered", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Working",  working, GreenWorking, Modifier.weight(1f))
                    StatCard("Check",    check,   AmberCheck,   Modifier.weight(1f))
                    StatCard("Repair",   repair,  RedRepair,    Modifier.weight(1f))
                }
            }

            // Quick actions
            item {
                Text("Quick Actions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard("Health\nCheck", Icons.Default.Checklist, Modifier.weight(1f)) {
                        navController.navigate("health_check")
                    }
                    QuickActionCard("Repair\nList", Icons.Default.Build, Modifier.weight(1f)) {
                        navController.navigate("repairs")
                    }
                    QuickActionCard("Summary\nReport", Icons.Default.Assessment, Modifier.weight(1f)) {
                        navController.navigate("reports")
                    }
                }
            }

            // Recent assets
            item {
                Text("Recent Assets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }

            if (assets.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(8.dp))
                            Text("No assets yet. Tap + to add one.", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(assets.take(10)) { asset ->
                    AssetCard(asset = asset) {
                        navController.navigate("asset_detail/${asset.id}")
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}
