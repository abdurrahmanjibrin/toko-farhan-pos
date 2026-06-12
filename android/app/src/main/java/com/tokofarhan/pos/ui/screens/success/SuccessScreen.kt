package com.tokofarhan.pos.ui.screens.success

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private fun formatTimestamp(ts: String): String = try {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(ts.substring(0, 19))
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(date!!)
} catch (_: Exception) { ts }

@Composable
fun SuccessScreen(
    transactionId: String,
    onNewTransaction: () -> Unit,
    vm: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(transactionId) {
        vm.loadDetail(transactionId)
    }

    val detail = vm.selectedDetail.value

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        // Success Icon
        Box(Modifier.size(100.dp).background(SageLight, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.CheckCircle, "Berhasil", modifier = Modifier.size(64.dp), tint = Primary)
        }
        Spacer(Modifier.height(24.dp))
        Text("Transaksi Berhasil!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Terima kasih atas pembelian Anda.", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        if (detail == null) {
            CircularProgressIndicator(color = Primary)
            Spacer(Modifier.weight(1f))
        } else {
            val tx = detail.first
            val items = detail.second
            val now = formatTimestamp(tx.timestamp)

            // Receipt Card
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Waktu Transaksi", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Text(now, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("No. Struk", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Text("#${tx.receiptNumber}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineVariant)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Belanja", style = MaterialTheme.typography.bodyLarge)
                        Text(rp.format(tx.totalAmount), style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tunai", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Text(rp.format(tx.paidAmount), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Text(rp.format(tx.changeAmount), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { printReceiptPdf(context, tx, items) },
                    Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, "Print", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cetak Struk")
                }
                OutlinedButton(
                    onClick = { shareReceiptWhatsApp(context, tx, items) },
                    Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kirim WA")
                }
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = onNewTransaction, Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Icon(Icons.Default.AddCircleOutline, "New")
                Spacer(Modifier.width(8.dp))
                Text("Mulai Transaksi Baru", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

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
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
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
