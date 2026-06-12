package com.tokofarhan.pos.ui.screens.history

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.tokofarhan.pos.data.local.entity.TransactionEntity
import com.tokofarhan.pos.data.local.entity.TransactionItemEntity
import com.tokofarhan.pos.ui.theme.*
import com.tokofarhan.pos.viewmodel.HistoryViewModel
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val rp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: HistoryViewModel = viewModel()) {
    val context = LocalContext.current
    val transactions = vm.transactions.value
    val grouped = transactions.groupBy { it.timestamp.substring(0, 10) }

    // Refresh on recompose
    LaunchedEffect(Unit) { vm.loadTransactions() }

    Column(Modifier.fillMaxSize().background(Background)) {
        CenterAlignedTopAppBar(
            title = { Text("Riwayat Transaksi", fontWeight = FontWeight.Bold) },
            actions = {
                // Filter date button
                if (vm.filterDate.value != null) {
                    IconButton(onClick = { vm.clearFilter() }) {
                        Icon(Icons.Default.FilterListOff, "Hapus Filter", tint = CriticalRed)
                    }
                }
                IconButton(onClick = { vm.showDatePicker.value = true }) {
                    Icon(Icons.Default.CalendarMonth, "Filter Tanggal")
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (transactions.isEmpty()) {
            // Empty state
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (vm.filterDate.value != null) "Tidak ada transaksi pada tanggal ini"
                        else "Belum ada riwayat transaksi",
                        style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant
                    )
                    if (vm.filterDate.value != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { vm.clearFilter() }) {
                            Text("Hapus Filter")
                        }
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                grouped.forEach { (date, txList) ->
                    item {
                        val label = formatDateLabel(date)
                        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp))
                    }
                    items(txList, key = { it.id }) { tx ->
                        TransactionCard(tx = tx, onClick = { vm.loadDetail(tx.id) })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (vm.showDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
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

    // Detail Bottom Sheet
    if (vm.showDetail.value) {
        val detail = vm.selectedDetail.value
        if (detail != null) {
            ModalBottomSheet(
                onDismissRequest = { vm.dismissDetail() },
                containerColor = Background,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Detail Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("#${detail.first.receiptNumber} • ${formatTimestamp(detail.first.timestamp)}",
                                style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                        IconButton(onClick = { vm.dismissDetail() }) { Icon(Icons.Default.Close, "Close") }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Payment method badge
                    Surface(color = BlueLight, shape = RoundedCornerShape(8.dp)) {
                        Text(detail.first.paymentMethod, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium, color = Secondary)
                    }
                    Spacer(Modifier.height(16.dp))
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Column(Modifier.padding(16.dp)) {
                            detail.second.forEachIndexed { idx, item ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.productName, style = MaterialTheme.typography.bodyLarge)
                                        Text("${item.qtyJual.toInt()} x ${rp.format(item.sellingPrice)}",
                                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                    }
                                    Text(rp.format(item.subtotal), style = MaterialTheme.typography.bodyLarge)
                                }
                                if (idx < detail.second.size - 1) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(rp.format(detail.first.totalAmount),
                                    style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bayar", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                Text(rp.format(detail.first.paidAmount), style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kembalian", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                Text(rp.format(detail.first.changeAmount), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // Action buttons — both now functional
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                printReceiptPdf(context, detail.first, detail.second)
                            },
                            Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Print, "Print", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cetak Struk")
                        }
                        OutlinedButton(
                            onClick = {
                                shareReceiptWhatsApp(context, detail.first, detail.second)
                            },
                            Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Kirim WA")
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: TransactionEntity, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(BlueLight, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, "Receipt", tint = Secondary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("#${tx.receiptNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatTime(tx.timestamp), style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Surface(color = SurfaceContainerHigh, shape = RoundedCornerShape(4.dp)) {
                        Text(tx.paymentMethod, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                }
            }
            Text(rp.format(tx.totalAmount), style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

// --- Helper functions ---

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

private fun formatTime(ts: String): String = try {
    val parts = ts.split("T"); if (parts.size > 1) parts[1].substring(0, 5) else ts
} catch (_: Exception) { ts }

private fun formatTimestamp(ts: String): String = try {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(ts.substring(0, 19))
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(date!!)
} catch (_: Exception) { ts }

// --- Print Receipt as PDF with full item details ---

private fun printReceiptPdf(
    context: Context, tx: TransactionEntity, items: List<TransactionItemEntity>
) {
    try {
        val dateTime = formatTimestamp(tx.timestamp)
        val document = PdfDocument()
        val pageWidth = 300
        // Dynamic height based on number of items, increased to accommodate wrapped names
        val pageHeight = 400 + (items.size * 80)
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val paintTitle = Paint().apply {
            color = Color.BLACK; textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val paintNormal = Paint().apply {
            color = Color.BLACK; textSize = 11f; typeface = Typeface.MONOSPACE
        }
        val paintRight = Paint().apply {
            color = Color.BLACK; textSize = 11f; typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.RIGHT
        }
        val paintLine = Paint().apply {
            color = Color.DKGRAY; textSize = 11f; typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }

        var y = 35f
        val cx = pageWidth / 2f
        val lx = 20f
        val rx = pageWidth - 20f

        canvas.drawText("TOKO FARHAN AL-BASYAR", cx, y, paintTitle); y += 28f
        canvas.drawText("================================", cx, y, paintLine); y += 22f
        canvas.drawText("Waktu   : $dateTime", lx, y, paintNormal); y += 18f
        canvas.drawText("No.Struk: #${tx.receiptNumber}", lx, y, paintNormal); y += 18f
        canvas.drawText("Metode  : ${tx.paymentMethod}", lx, y, paintNormal); y += 22f
        canvas.drawText("--------------------------------", cx, y, paintLine); y += 20f

        // Item details
        items.forEach { item ->
            val name = item.productName
            var currentLine = ""
            name.split(" ").forEach { word ->
                if (currentLine.length + word.length + 1 <= 35) {
                    currentLine += (if (currentLine.isEmpty()) "" else " ") + word
                } else {
                    canvas.drawText(currentLine, lx, y, paintNormal); y += 16f
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, lx, y, paintNormal); y += 16f
            }

            canvas.drawText("  ${item.qtyJual.toInt()} x ${rp.format(item.sellingPrice)}", lx, y, paintNormal)
            canvas.drawText(rp.format(item.subtotal), rx, y, paintRight); y += 20f
        }

        canvas.drawText("--------------------------------", cx, y, paintLine); y += 22f
        canvas.drawText("Total Belanja:", lx, y, paintNormal)
        canvas.drawText(rp.format(tx.totalAmount), rx, y, paintRight); y += 18f
        canvas.drawText("Tunai:", lx, y, paintNormal)
        canvas.drawText(rp.format(tx.paidAmount), rx, y, paintRight); y += 18f
        canvas.drawText("Kembalian:", lx, y, paintNormal)
        canvas.drawText(rp.format(tx.changeAmount), rx, y, paintRight); y += 22f
        canvas.drawText("--------------------------------", cx, y, paintLine); y += 22f
        canvas.drawText("Terima kasih atas pembelian", cx, y, paintLine); y += 16f
        canvas.drawText("Anda!", cx, y, paintLine); y += 22f
        canvas.drawText("================================", cx, y, paintLine)

        document.finishPage(page)

        val file = File(context.cacheDir, "struk_${tx.receiptNumber}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Struk Toko FARHAN AL-BASYAR - #${tx.receiptNumber}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Cetak / Bagikan Struk"))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuat struk: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// --- Share Receipt via WhatsApp ---

private fun shareReceiptWhatsApp(
    context: Context, tx: TransactionEntity, items: List<TransactionItemEntity>
) {
    val dateTime = formatTimestamp(tx.timestamp)
    val message = buildString {
        appendLine("🧾 *STRUK TOKO FARHAN AL-BASYAR*")
        appendLine()
        appendLine("📅 Waktu: $dateTime")
        appendLine("🔢 No. Struk: #${tx.receiptNumber}")
        appendLine("💳 Metode: ${tx.paymentMethod}")
        appendLine()
        appendLine("*Detail Pembelian:*")
        items.forEach { item ->
            appendLine("• ${item.productName}")
            appendLine("  ${item.qtyJual.toInt()} x ${rp.format(item.sellingPrice)} = ${rp.format(item.subtotal)}")
        }
        appendLine()
        appendLine("💰 *Total: ${rp.format(tx.totalAmount)}*")
        appendLine("💵 Tunai: ${rp.format(tx.paidAmount)}")
        appendLine("💱 Kembalian: ${rp.format(tx.changeAmount)}")
        appendLine()
        appendLine("Terima kasih atas pembelian Anda! 🙏")
    }

    try {
        val waIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(waIntent)
    } catch (e: Exception) {
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val waUrl = "https://wa.me/?text=$encodedMessage"
            val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(waUrl))
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Kirim Struk via..."))
        }
    }
}
