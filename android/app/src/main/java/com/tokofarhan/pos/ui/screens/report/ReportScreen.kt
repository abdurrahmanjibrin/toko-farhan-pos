package com.tokofarhan.pos.ui.screens.report

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tokofarhan.pos.ui.theme.*
import com.tokofarhan.pos.viewmodel.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val rp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(vm: ReportViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Refresh on recompose
    LaunchedEffect(Unit) { vm.loadReport() }

    // Show export status toast
    val exportStatus = vm.exportStatus.value
    LaunchedEffect(exportStatus) {
        exportStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.exportStatus.value = null
        }
    }

    Column(Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState())) {
        CenterAlignedTopAppBar(
            title = { Text("Laporan", fontWeight = FontWeight.Bold) },
            actions = {
                if (vm.filterDate.value != null) {
                    IconButton(onClick = { vm.clearFilter() }) {
                        Icon(Icons.Default.FilterListOff, "Hapus Filter", tint = CriticalRed)
                    }
                }
                IconButton(onClick = { vm.showDatePicker.value = true }) {
                    Icon(Icons.Default.CalendarMonth, "Filter Tanggal")
                }
                IconButton(onClick = { vm.loadReport() }) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
        )

        // Active filter chip
        vm.filterDate.value?.let { date ->
            Surface(
                color = SageLight, shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp), tint = Primary)
                    Spacer(Modifier.width(6.dp))
                    Text("Filter: ${formatDateLabel(date)}", style = MaterialTheme.typography.labelMedium, color = Primary)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { vm.clearFilter() }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, "Hapus", modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                    }
                }
            }
        }

        if (vm.isLoading.value) {
            Box(Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (vm.dailyEntries.value.isEmpty()) {
            // Empty state
            Box(Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Assessment, null, modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("Belum ada data laporan", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                    Text("Mulai transaksi untuk melihat laporan", style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        } else {
            Column(Modifier.padding(horizontal = 20.dp)) {
                // Today's Sales Card
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PENJUALAN HARI INI", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                            Icon(Icons.Default.TrendingUp, null, tint = Primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(rp.format(vm.todayRevenue), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("Laba hari ini: ${rp.format(vm.todayProfit)}", style = MaterialTheme.typography.bodyMedium, color = Primary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Summary cards row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Total Laba", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(rp.format(vm.totalProfit), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                            Text("${vm.dailyEntries.value.size} hari", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            if (vm.lowStockCount.value > 0) vm.showLowStockDialog.value = true
                        },
                        shape = RoundedCornerShape(16.dp), 
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Stok Hampir Habis", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (vm.lowStockCount.value > 0) {
                                    Icon(Icons.Default.Warning, null, tint = CriticalRed, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text("${vm.lowStockCount.value} Item", style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vm.lowStockCount.value > 0) CriticalRed else OnSurface)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Chart Card
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Grafik Penjualan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Setiap Harinya", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                            }
                            Surface(color = SageLight, shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(Primary, RoundedCornerShape(4.dp)))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pendapatan", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        BarChart(data = vm.chartData, modifier = Modifier.fillMaxWidth().height(220.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Daily breakdown table
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Rincian Harian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        // Header
                        Row(Modifier.fillMaxWidth()) {
                            Text("Tanggal", Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                            Text("Trx", Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                            Text("Pendapatan", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant, textAlign = TextAlign.End)
                            Text("Laba", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant, textAlign = TextAlign.End)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        vm.filteredEntries.reversed().forEach { entry ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(entry.date.substring(5), Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall)
                                Text("${entry.totalTransactions}", Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                Text(rp.format(entry.totalRevenue), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                                Text(rp.format(entry.totalProfit), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End, color = if (entry.totalProfit >= 0) Primary else CriticalRed)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Export buttons
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { 
                            coroutineScope.launch { saveReportToDownloads(context, vm) }
                        },
                        Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Icon(Icons.Default.SaveAlt, "Save", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan Excel")
                    }
                    OutlinedButton(
                        onClick = { 
                            coroutineScope.launch { shareReportExcel(context, vm) }
                        },
                        Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary)
                    ) {
                        Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Bagikan")
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Date Picker Dialog
    if (vm.showDatePicker.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { vm.showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = sdf.format(Date(millis))
                        vm.filterByDate(date)
                    }
                    vm.showDatePicker.value = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { vm.showDatePicker.value = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Low Stock Dialog
    if (vm.showLowStockDialog.value) {
        ReportLowStockDialog(vm = vm)
    }
}

// Save Excel directly to Downloads folder
private suspend fun saveReportToDownloads(context: Context, vm: ReportViewModel) {
    try {
        val transactions = vm.getAllTransactionsForExport()
        if (transactions.isEmpty()) {
            vm.exportStatus.value = "Belum ada transaksi untuk di-export"
            return
        }
        vm.exportStatus.value = "Membuat file Excel..."

        withContext(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Laporan_TokoFarhan_$dateStr.xls"
            val workbook = generateExcelWorkbook(transactions)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.ms-excel")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        workbook.write(outputStream)
                    }
                } else {
                    throw Exception("Gagal membuat file di Downloads")
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }
            }
            workbook.close()
            vm.exportStatus.value = "✅ Excel disimpan ke Downloads: $fileName"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        vm.exportStatus.value = "Gagal menyimpan: ${e.message}"
    }
}

// Share Excel via intent
private suspend fun shareReportExcel(context: Context, vm: ReportViewModel) {
    try {
        val transactions = vm.getAllTransactionsForExport()
        if (transactions.isEmpty()) {
            vm.exportStatus.value = "Belum ada transaksi untuk dibagikan"
            return
        }
        vm.exportStatus.value = "Membuat file Excel..."

        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Laporan_TokoFarhan_$dateStr.xls"
        val file = File(context.cacheDir, fileName)

        withContext(Dispatchers.IO) {
            val workbook = generateExcelWorkbook(transactions)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Toko FARHAN AL-BASYAR - $dateStr")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        vm.exportStatus.value = null // clear status
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Excel Laporan"))
    } catch (e: Exception) {
        e.printStackTrace()
        vm.exportStatus.value = "Gagal membagikan: ${e.message}"
    }
}

// Generate Apache POI Workbook with sheets grouped by month
private fun generateExcelWorkbook(
    transactions: List<Pair<com.tokofarhan.pos.data.local.entity.TransactionEntity, List<com.tokofarhan.pos.data.local.entity.TransactionItemEntity>>>
): HSSFWorkbook {
    val workbook = HSSFWorkbook()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))

    // Group transactions by "Month Year" (e.g., "Mei 2026")
    val groupedByMonth = transactions.groupBy { 
        try {
            val date = dateFormat.parse(it.first.timestamp.substring(0, 19))
            monthFormat.format(date!!)
        } catch (e: Exception) {
            "Lainnya"
        }
    }

    // Sort months descending (assuming keys can be parsed back or just trust chronological insertion if sorting by latest, 
    // but the query is ASC, so keys are chronologically ascending. We will just iterate.)
    groupedByMonth.forEach { (monthName, txList) ->
        // Create sheet for this month
        val sheet = workbook.createSheet(monthName)
        
        // Header Row
        val headerRow = sheet.createRow(0)
        val headers = listOf("Tanggal", "No Struk", "Metode Bayar", "Nama Barang", "Qty", "Harga Satuan", "HPP Satuan", "Subtotal", "Laba")
        headers.forEachIndexed { index, title ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(title)
        }

        var rowIdx = 1
        var totalBulanIni = 0.0
        var labaBulanIni = 0.0

        txList.forEach { (tx, items) ->
            val txDate = try {
                displayFormat.format(dateFormat.parse(tx.timestamp.substring(0, 19))!!)
            } catch (e: Exception) { tx.timestamp }

            items.forEach { item ->
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).setCellValue(txDate)
                row.createCell(1).setCellValue(tx.receiptNumber)
                row.createCell(2).setCellValue(tx.paymentMethod)
                row.createCell(3).setCellValue(item.productName)
                row.createCell(4).setCellValue(item.qtyJual)
                row.createCell(5).setCellValue(item.sellingPrice)
                row.createCell(6).setCellValue(item.recordedHpp)
                row.createCell(7).setCellValue(item.subtotal)
                row.createCell(8).setCellValue(item.profit)

                totalBulanIni += item.subtotal
                labaBulanIni += item.profit
            }
        }

        // Summary row at the bottom of the sheet
        val summaryRow = sheet.createRow(rowIdx + 1)
        summaryRow.createCell(6).setCellValue("TOTAL BULAN INI")
        summaryRow.createCell(7).setCellValue(totalBulanIni)
        summaryRow.createCell(8).setCellValue(labaBulanIni)

        // Set reasonable column widths manually (autoSizeColumn uses AWT which is unavailable on Android)
        val columnWidths = intArrayOf(5000, 5000, 3500, 8000, 2000, 4000, 4000, 4000, 4000)
        for (i in headers.indices) {
            sheet.setColumnWidth(i, columnWidths.getOrElse(i) { 4000 })
        }
    }

    return workbook
}

@Composable
private fun BarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("Belum ada data", color = OnSurfaceVariant)
        }
        return
    }
    val maxVal = data.maxOf { it.second }.coerceAtLeast(1.0)
    val barColor = ChartBar
    val todayColor = ChartBarToday
    val density = LocalDensity.current

    // Width per bar slot (bar + gap)
    val barSlotWidthDp = 48.dp
    val totalWidthDp = barSlotWidthDp * data.size
    val scrollState = rememberScrollState()

    // Tooltip state
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                Modifier
                    .width(totalWidthDp)
                    .fillMaxHeight()
                    .pointerInput(data) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                val slotWidthPx = with(density) { barSlotWidthDp.toPx() }
                                val index = (offset.x / slotWidthPx).toInt()
                                if (index in data.indices) {
                                    selectedIndex = index
                                }
                            },
                            onPress = {
                                tryAwaitRelease()
                                selectedIndex = -1
                            },
                            onTap = { offset ->
                                val slotWidthPx = with(density) { barSlotWidthDp.toPx() }
                                val index = (offset.x / slotWidthPx).toInt()
                                selectedIndex = if (selectedIndex == index) -1 else index
                            }
                        )
                    }
            ) {
                val slotWidth = size.width / data.size
                val barWidth = slotWidth * 0.6f
                val gap = (slotWidth - barWidth) / 2f

                data.forEachIndexed { i, (_, value) ->
                    val barHeight = (value / maxVal * size.height * 0.80f).toFloat()
                    val x = i * slotWidth + gap
                    val isLast = i == data.size - 1
                    drawRoundRect(
                        color = if (isLast) todayColor else barColor,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Draw tooltip for selected bar
                    if (selectedIndex == i) {
                        val text = rp.format(value)
                        val paint = android.graphics.Paint().apply {
                            textSize = 28f
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        }
                        val textWidth = paint.measureText(text)
                        val bgPadH = 16f
                        val bgPadV = 10f
                        val bgWidth = textWidth + bgPadH * 2
                        val bgHeight = 40f
                        val tooltipX = x + barWidth / 2
                        val tooltipY = (size.height - barHeight - 16f).coerceAtLeast(bgHeight + 4f)

                        // Background rounded rect
                        drawContext.canvas.nativeCanvas.drawRoundRect(
                            tooltipX - bgWidth / 2,
                            tooltipY - bgHeight,
                            tooltipX + bgWidth / 2,
                            tooltipY,
                            12f, 12f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#333333")
                                isAntiAlias = true
                                style = android.graphics.Paint.Style.FILL
                                setShadowLayer(8f, 0f, 4f, android.graphics.Color.argb(80, 0, 0, 0))
                            }
                        )
                        // Text
                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            tooltipX,
                            tooltipY - bgPadV,
                            paint
                        )
                    }
                }
            }
        }

        // Date labels — horizontally scrollable in sync
        Row(
            Modifier
                .width(totalWidthDp)
                .horizontalScroll(scrollState)
        ) {
            data.forEach { (label, _) ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(barSlotWidthDp)
                )
            }
        }
    }
}

// --- Helper function ---

private fun formatDateLabel(isoDate: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(isoDate) ?: return isoDate
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        cal.time = date
        when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
                "Hari Ini, ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(date)}"
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 ->
                "Kemarin, ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(date)}"
            else -> SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(date)
        }
    } catch (_: Exception) { isoDate }
}

@Composable
private fun ReportLowStockDialog(vm: ReportViewModel) {
    val lowStockProducts = vm.lowStockProducts.value

    AlertDialog(
        onDismissRequest = { vm.showLowStockDialog.value = false },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = CriticalRed)
                Spacer(Modifier.width(8.dp))
                Text("Stok Mau Habis", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (lowStockProducts.isEmpty()) {
                Text("Semua stok barang masih aman.", color = OnSurfaceVariant)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lowStockProducts, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text("Sisa: ${product.currentStokJual.toInt()}", style = MaterialTheme.typography.bodyMedium, color = CriticalRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.showLowStockDialog.value = false }) {
                Text("Tutup", color = Primary)
            }
        }
    )
}
