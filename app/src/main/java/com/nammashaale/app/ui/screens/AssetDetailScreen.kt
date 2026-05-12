package com.nammashaale.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nammashaale.app.data.Asset
import com.nammashaale.app.ui.components.ConditionBadge
import com.nammashaale.app.ui.components.conditionColor
import com.nammashaale.app.ui.components.formatDate
import com.nammashaale.app.viewmodel.AssetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    navController: NavController,
    assetId: Int,
    vm: AssetViewModel = viewModel()
) {
    var asset by remember { mutableStateOf<Asset?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(assetId) {
        vm.getAssetById(assetId) { asset = it }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon  = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Asset?") },
            text  = { Text("This will permanently delete '${asset?.name}' from the inventory.") },
            confirmButton = {
                TextButton(onClick = {
                    asset?.let { vm.deleteAsset(it) }
                    navController.popBackStack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(asset?.name ?: "Asset Detail", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        asset?.let { a ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Photo ────────────────────────────────────────────────
                val photoSource = a.photoUrl ?: a.photoPath
                if (!photoSource.isNullOrBlank()) {
                    AsyncImage(
                        model = photoSource,
                        contentDescription = "Asset photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // ── Condition + Date ─────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ConditionBadge(a.condition)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Last checked: ${formatDate(a.lastChecked)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                HorizontalDivider()

                // ── Detail Rows ──────────────────────────────────────────
                DetailRow(Icons.Default.Label,      "Name",          a.name)
                DetailRow(Icons.Default.Tag,        "Serial Number", a.serialNumber)
                DetailRow(Icons.Default.LocationOn, "Location",      a.location)
                DetailRow(Icons.Default.Category,   "Category",      a.category)

                if (!a.issueNote.isNullOrBlank()) {
                    DetailRow(Icons.Default.Warning, "Issue Note", a.issueNote)
                }

                HorizontalDivider()

                // ── Quick Condition Update ───────────────────────────────
                Text(
                    "Update Condition",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Working", "Needs Check", "Needs Repair").forEach { cond ->
                        val color      = conditionColor(cond)
                        val isSelected = a.condition == cond

                        OutlinedButton(
                            onClick = {
                                val updated = a.copy(
                                    condition   = cond,
                                    lastChecked = System.currentTimeMillis()
                                )
                                vm.updateAsset(updated)
                                asset = updated
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.15f)
                                                 else MaterialTheme.colorScheme.surface,
                                contentColor   = color
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = color.copy(alpha = if (isSelected) 1f else 0.5f)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text  = cond.replace("Needs ", ""),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        } ?: Box(
            modifier          = Modifier.fillMaxSize().padding(padding),
            contentAlignment  = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(20.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
        }
    }
}
