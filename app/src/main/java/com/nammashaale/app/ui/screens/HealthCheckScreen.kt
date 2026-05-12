package com.nammashaale.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammashaale.app.data.Asset
import com.nammashaale.app.ui.theme.AmberCheck
import com.nammashaale.app.ui.theme.GreenWorking
import com.nammashaale.app.ui.theme.RedRepair
import com.nammashaale.app.viewmodel.AssetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(
    navController: NavController,
    vm: AssetViewModel = viewModel()
) {
    val assets by vm.assets.collectAsState()
    var updatedCount by remember { mutableStateOf(0) }
    var showDoneDialog by remember { mutableStateOf(false) }

    if (showDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDoneDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenWorking) },
            title = { Text("Health Check Complete!") },
            text  = { Text("$updatedCount asset(s) updated. Report is ready to generate.") },
            confirmButton = {
                TextButton(onClick = { showDoneDialog = false; navController.popBackStack() }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDoneDialog = false; navController.navigate("reports") }) {
                    Text("View Report")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Monthly Health Check", fontWeight = FontWeight.SemiBold)
                        Text("Tap condition for each item", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDoneDialog = true },
                icon = { Icon(Icons.Default.Done, contentDescription = null) },
                text = { Text("Done ($updatedCount updated)") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    "${assets.size} assets to check",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            items(assets, key = { it.id }) { asset ->
                HealthCheckRow(
                    asset = asset,
                    onConditionChange = { newCondition ->
                        vm.updateAsset(asset.copy(
                            condition = newCondition,
                            lastChecked = System.currentTimeMillis()
                        ))
                        updatedCount++
                    }
                )
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Composable
fun HealthCheckRow(asset: Asset, onConditionChange: (String) -> Unit) {
    var selected by remember(asset.condition) { mutableStateOf(asset.condition) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(asset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${asset.serialNumber} · ${asset.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConditionButton("Working", GreenWorking, selected == "Working") {
                    selected = "Working"
                    onConditionChange("Working")
                }
                ConditionButton("Check", AmberCheck, selected == "Needs Check") {
                    selected = "Needs Check"
                    onConditionChange("Needs Check")
                }
                ConditionButton("Repair", RedRepair, selected == "Needs Repair") {
                    selected = "Needs Repair"
                    onConditionChange("Needs Repair")
                }
            }
        }
    }
}

@Composable
fun ConditionButton(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) color else color.copy(alpha = 0.1f)
    val textColor = if (selected) Color.White else color

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = textColor),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
