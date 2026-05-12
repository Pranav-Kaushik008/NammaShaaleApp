package com.nammashaale.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nammashaale.app.data.Asset
import com.nammashaale.app.ui.theme.AmberCheck
import com.nammashaale.app.ui.theme.GreenWorking
import com.nammashaale.app.ui.theme.RedRepair
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Bottom Navigation ────────────────────────────────────────────────────────

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

val bottomNavItems = listOf(
    BottomNavItem("Home",    Icons.Default.Home,     "dashboard"),
    BottomNavItem("Assets",  Icons.Default.Inventory, "asset_list"),
    BottomNavItem("Repairs", Icons.Default.Build,    "repairs"),
    BottomNavItem("Reports", Icons.Default.Assessment,"reports")
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick  = {
                    if (current != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon  = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

@Composable
fun StatCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(82.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "$count",
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ─── Asset Card ───────────────────────────────────────────────────────────────

@Composable
fun AssetCard(asset: Asset, onClick: () -> Unit) {
    val conditionColor = conditionColor(asset.condition)
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp).height(56.dp)
                    .background(conditionColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(asset.serialNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("📍 ${asset.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(
                    text  = "Last checked: ${formatDate(asset.lastChecked)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            ConditionBadge(asset.condition)
        }
    }
}

// ─── Condition Badge ──────────────────────────────────────────────────────────

@Composable
fun ConditionBadge(condition: String) {
    val color = conditionColor(condition)
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text     = condition,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

fun conditionColor(condition: String): Color = when (condition) {
    "Working"     -> GreenWorking
    "Needs Check" -> AmberCheck
    else          -> RedRepair
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
