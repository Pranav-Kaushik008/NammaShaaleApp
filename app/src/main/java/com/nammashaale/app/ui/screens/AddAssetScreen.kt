package com.nammashaale.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nammashaale.app.data.Asset
import com.nammashaale.app.viewmodel.AssetViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    navController: NavController,
    vm: AssetViewModel = viewModel()
) {
    val context = LocalContext.current

    var name              by remember { mutableStateOf("") }
    var serialNo          by remember { mutableStateOf("") }
    var location          by remember { mutableStateOf("") }
    var condition         by remember { mutableStateOf("Working") }
    var category          by remember { mutableStateOf("General") }
    var issueNote         by remember { mutableStateOf("") }
    var photoUri          by remember { mutableStateOf<Uri?>(null) }
    var conditionExpanded by remember { mutableStateOf(false) }
    var categoryExpanded  by remember { mutableStateOf(false) }
    var showPermDenied    by remember { mutableStateOf(false) }

    val conditions = listOf("Working", "Needs Check", "Needs Repair")
    val categories = listOf("General", "Lab", "Sports", "Tablet", "Furniture", "Other")

    // Must create URI before launching camera, store in state
    val pendingUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = pendingUri.value
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            pendingUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            showPermDenied = true
        }
    }

    if (showPermDenied) {
        AlertDialog(
            onDismissRequest = { showPermDenied = false },
            icon  = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Camera Permission Needed") },
            text  = { Text("Camera access is required to document asset condition. Please allow it in App Settings.") },
            confirmButton = {
                TextButton(onClick = { showPermDenied = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Asset", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Photo Preview ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Asset photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No photo yet", color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (photoUri != null) "Retake Photo" else "Take Photo")
            }

            HorizontalDivider()

            // ── Form Fields ─────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Asset Name *") },
                placeholder = { Text("e.g. Microscope, Football, Tablet") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                singleLine = true
            )

            OutlinedTextField(
                value = serialNo,
                onValueChange = { serialNo = it },
                label = { Text("Serial Number *") },
                placeholder = { Text("e.g. MIC-2024-003") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                placeholder = { Text("e.g. Science Lab, Sports Room") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true
            )

            // ── Condition Dropdown ──────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = conditionExpanded,
                onExpandedChange = { conditionExpanded = it }
            ) {
                OutlinedTextField(
                    value = condition,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Condition") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )
                ExposedDropdownMenu(
                    expanded = conditionExpanded,
                    onDismissRequest = { conditionExpanded = false }
                ) {
                    conditions.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = { condition = c; conditionExpanded = false }
                        )
                    }
                }
            }

            // ── Category Dropdown ───────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = { category = c; categoryExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = issueNote,
                onValueChange = { issueNote = it },
                label = { Text("Issue Note (optional)") },
                placeholder = { Text("Describe any issue with this asset...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(Modifier.height(4.dp))

            val isFormValid = name.isNotBlank() && serialNo.isNotBlank() && location.isNotBlank()

            if (!isFormValid) {
                Text(
                    "* Name, Serial Number and Location are required",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Button(
                onClick = {
                    val asset = Asset(
                        name         = name.trim(),
                        serialNumber = serialNo.trim(),
                        location     = location.trim(),
                        condition    = condition,
                        category     = category,
                        issueNote    = issueNote.takeIf { it.isNotBlank() },
                        photoPath    = photoUri?.toString()
                    )
                    vm.addAsset(asset, photoUri)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = isFormValid
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Asset", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

fun createImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "asset_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
