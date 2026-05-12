package com.nammashaale.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammashaale.app.data.Asset
import com.nammashaale.app.ui.components.BottomNavBar
import com.nammashaale.app.ui.components.formatDate
import com.nammashaale.app.ui.theme.AmberCheck
import com.nammashaale.app.ui.theme.GreenWorking
import com.nammashaale.app.ui.theme.RedRepair
import com.nammashaale.app.viewmodel.AssetViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    vm: AssetViewModel = viewModel()
) {
    val context = LocalContext.current
    val assets  by vm.assets.collectAsState()
    val total   by vm.totalCount.collectAsState()
    val working by vm.workingCount.collectAsState()
    val check   by vm.checkCount.collectAsState()
    val repair  by vm.repairCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary Report", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("NAMMA-SHAALE INVENTORY REPORT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("GPS Higher Primary School", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("Generated: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Stats
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportStatCard("Total",   total,   MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                ReportStatCard("Working", working, GreenWorking, Modifier.weight(1f))
                ReportStatCard("Check",   check,   AmberCheck,   Modifier.weight(1f))
                ReportStatCard("Repair",  repair,  RedRepair,    Modifier.weight(1f))
            }

            // Repair list section
            if (repair > 0) {
                Text("Items Needing Repair", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                assets.filter { it.condition == "Needs Repair" }.forEach { asset ->
                    RepairReportRow(asset)
                }
            }

            // Needs check section
            if (check > 0) {
                Text("Items Needing Check", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                assets.filter { it.condition == "Needs Check" }.forEach { asset ->
                    RepairReportRow(asset)
                }
            }

            // Category breakdown
            Text("Category Breakdown", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            val categories = assets.groupBy { it.category }
            categories.forEach { (cat, list) ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat, fontWeight = FontWeight.Medium)
                        Text("${list.size} item(s)", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            HorizontalDivider()

            // Download PDF button
            Button(
                onClick = {
                    val file = generatePdfReport(context, assets, total, working, check, repair)
                    if (file != null) {
                        Toast.makeText(context, "PDF saved to Downloads!", Toast.LENGTH_SHORT).show()
                        sharePdf(context, file)
                    } else {
                        Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Download PDF Report", style = MaterialTheme.typography.labelLarge)
            }

            // Share as text button
            OutlinedButton(
                onClick = {
                    val reportText = generateReportText(assets, total, working, check, repair)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Namma-Shaale Inventory Report")
                        putExtra(Intent.EXTRA_TEXT, reportText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share as Text", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReportStatCard(label: String, value: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun RepairReportRow(asset: Asset) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(asset.name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                Text("${asset.serialNumber} · ${asset.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                if (!asset.issueNote.isNullOrBlank()) {
                    Text("Note: ${asset.issueNote}", style = MaterialTheme.typography.labelSmall, color = RedRepair)
                }
            }
            Text(formatDate(asset.lastChecked), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// ─── PDF Generation ───────────────────────────────────────────────────────────

fun generatePdfReport(
    context: Context,
    assets: List<Asset>,
    total: Int,
    working: Int,
    check: Int,
    repair: Int
): File? {
    return try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595  // A4 width in points
        val pageHeight = 842 // A4 height in points
        val margin = 40f
        val usableWidth = pageWidth - 2 * margin

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin + 10f

        // Paints
        val titlePaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.parseColor("#6C63FF")
        }
        val subtitlePaint = Paint().apply {
            textSize = 12f
            color = AndroidColor.GRAY
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.parseColor("#1A1A2E")
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            color = AndroidColor.parseColor("#333333")
        }
        val boldBodyPaint = Paint().apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.parseColor("#333333")
        }
        val notePaint = Paint().apply {
            textSize = 10f
            color = AndroidColor.parseColor("#E53935")
        }
        val linePaint = Paint().apply {
            color = AndroidColor.parseColor("#CCCCCC")
            strokeWidth = 1f
        }
        val boxPaint = Paint().apply {
            color = AndroidColor.parseColor("#F0F0FF")
            style = Paint.Style.FILL
        }
        val greenPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.parseColor("#4CAF50")
        }
        val amberPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.parseColor("#FFA726")
        }
        val redPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.parseColor("#E53935")
        }

        // Helper to check if we need a new page
        fun ensureSpace(needed: Float): Canvas {
            if (yPos + needed > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                yPos = margin + 10f
                return page.canvas
            }
            return canvas
        }

        // ── Title Section ──
        canvas.drawText("NAMMA-SHAALE", margin, yPos, titlePaint)
        yPos += 24f
        canvas.drawText("INVENTORY REPORT", margin, yPos, titlePaint)
        yPos += 20f
        canvas.drawText("GPS Higher Primary School", margin, yPos, subtitlePaint)
        yPos += 16f
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated: $dateStr", margin, yPos, subtitlePaint)
        yPos += 10f

        // Divider
        yPos += 8f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        // ── Summary Stats Box ──
        canvas.drawRoundRect(margin, yPos - 5f, pageWidth - margin, yPos + 65f, 8f, 8f, boxPaint)

        val statWidth = usableWidth / 4f
        val stats = listOf(
            Triple("Total", total, headerPaint),
            Triple("Working", working, greenPaint),
            Triple("Needs Check", check, amberPaint),
            Triple("Needs Repair", repair, redPaint)
        )
        stats.forEachIndexed { i, (label, value, paint) ->
            val x = margin + i * statWidth + statWidth / 2f
            val valText = "$value"
            val valWidth = paint.measureText(valText)
            canvas.drawText(valText, x - valWidth / 2f, yPos + 25f, paint)
            val labelWidth = subtitlePaint.measureText(label)
            canvas.drawText(label, x - labelWidth / 2f, yPos + 45f, subtitlePaint)
        }
        yPos += 80f

        // ── Items Needing Repair ──
        val repairItems = assets.filter { it.condition == "Needs Repair" }
        if (repairItems.isNotEmpty()) {
            canvas = ensureSpace(30f)
            canvas.drawText("ITEMS NEEDING REPAIR", margin, yPos, headerPaint)
            yPos += 6f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 14f

            repairItems.forEach { asset ->
                canvas = ensureSpace(50f)
                canvas.drawText("• ${asset.name}", margin + 8f, yPos, boldBodyPaint)
                yPos += 15f
                canvas.drawText("SN: ${asset.serialNumber}  |  Location: ${asset.location}", margin + 16f, yPos, bodyPaint)
                yPos += 14f
                if (!asset.issueNote.isNullOrBlank()) {
                    canvas.drawText("Note: ${asset.issueNote}", margin + 16f, yPos, notePaint)
                    yPos += 14f
                }
                canvas.drawText("Last Checked: ${formatDate(asset.lastChecked)}", margin + 16f, yPos, subtitlePaint)
                yPos += 18f
            }
            yPos += 8f
        }

        // ── Items Needing Check ──
        val checkItems = assets.filter { it.condition == "Needs Check" }
        if (checkItems.isNotEmpty()) {
            canvas = ensureSpace(30f)
            canvas.drawText("ITEMS NEEDING CHECK", margin, yPos, headerPaint)
            yPos += 6f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 14f

            checkItems.forEach { asset ->
                canvas = ensureSpace(40f)
                canvas.drawText("• ${asset.name}", margin + 8f, yPos, boldBodyPaint)
                yPos += 15f
                canvas.drawText("SN: ${asset.serialNumber}  |  Location: ${asset.location}", margin + 16f, yPos, bodyPaint)
                yPos += 14f
                canvas.drawText("Last Checked: ${formatDate(asset.lastChecked)}", margin + 16f, yPos, subtitlePaint)
                yPos += 18f
            }
            yPos += 8f
        }

        // ── Category Breakdown ──
        val categories = assets.groupBy { it.category }
        if (categories.isNotEmpty()) {
            canvas = ensureSpace(30f)
            canvas.drawText("CATEGORY BREAKDOWN", margin, yPos, headerPaint)
            yPos += 6f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 14f

            categories.forEach { (cat, list) ->
                canvas = ensureSpace(20f)
                canvas.drawText("$cat:", margin + 8f, yPos, boldBodyPaint)
                val countText = "${list.size} item(s)"
                val countWidth = bodyPaint.measureText(countText)
                canvas.drawText(countText, pageWidth - margin - countWidth, yPos, bodyPaint)
                yPos += 18f
            }
            yPos += 8f
        }

        // ── All Assets Table ──
        canvas = ensureSpace(30f)
        canvas.drawText("COMPLETE ASSET LIST", margin, yPos, headerPaint)
        yPos += 6f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 14f

        // Table header
        canvas = ensureSpace(20f)
        val col1 = margin + 4f
        val col2 = margin + 160f
        val col3 = margin + 280f
        val col4 = margin + 400f
        canvas.drawText("Name", col1, yPos, boldBodyPaint)
        canvas.drawText("Serial No.", col2, yPos, boldBodyPaint)
        canvas.drawText("Location", col3, yPos, boldBodyPaint)
        canvas.drawText("Condition", col4, yPos, boldBodyPaint)
        yPos += 4f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 14f

        assets.forEach { asset ->
            canvas = ensureSpace(18f)
            // Truncate long text to fit columns
            canvas.drawText(asset.name.take(22), col1, yPos, bodyPaint)
            canvas.drawText(asset.serialNumber.take(16), col2, yPos, bodyPaint)
            canvas.drawText(asset.location.take(16), col3, yPos, bodyPaint)
            canvas.drawText(asset.condition, col4, yPos, bodyPaint)
            yPos += 16f
        }

        // ── Footer ──
        yPos += 16f
        canvas = ensureSpace(30f)
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 16f
        canvas.drawText("Generated by Namma-Shaale Inventory App", margin, yPos, subtitlePaint)

        // Finish the last page
        pdfDocument.finishPage(page)

        // Save the PDF
        val fileName = "NammaShaale_Report_${SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun sharePdf(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Namma-Shaale Inventory Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun generateReportText(assets: List<Asset>, total: Int, working: Int, check: Int, repair: Int): String {
    val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    val repairItems = assets.filter { it.condition == "Needs Repair" }
        .joinToString("\n") { "  - ${it.name} (${it.serialNumber}) @ ${it.location}${if (!it.issueNote.isNullOrBlank()) " | Note: ${it.issueNote}" else ""}" }
    val checkItems = assets.filter { it.condition == "Needs Check" }
        .joinToString("\n") { "  - ${it.name} (${it.serialNumber}) @ ${it.location}" }

    return """
NAMMA-SHAALE INVENTORY REPORT
GPS Higher Primary School
Date: $date
=====================================
SUMMARY
  Total Assets : $total
  Working      : $working
  Needs Check  : $check
  Needs Repair : $repair
=====================================
${if (repairItems.isNotBlank()) "ITEMS NEEDING REPAIR (SDMC Action Required):\n$repairItems\n" else ""}
${if (checkItems.isNotBlank()) "ITEMS NEEDING CHECK:\n$checkItems\n" else ""}
=====================================
Generated by Namma-Shaale Inventory App
    """.trimIndent()
}
