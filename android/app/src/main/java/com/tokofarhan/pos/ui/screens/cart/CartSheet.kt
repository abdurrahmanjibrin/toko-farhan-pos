package com.tokofarhan.pos.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.tokofarhan.pos.data.model.CartItem
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.tokofarhan.pos.R
import com.tokofarhan.pos.ui.theme.*
import com.tokofarhan.pos.viewmodel.PosViewModel
import java.text.NumberFormat
import java.util.Locale

private val rp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(vm: PosViewModel, navController: NavController) {
    var showPaymentDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { vm.showCart.value = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.showCart.value = false }) { Icon(Icons.Default.Close, "Tutup") }
                Text("Keranjang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                // "Hapus Semua" button — replaces the old non-functional "Siap" button
                if (vm.cartItems.isNotEmpty()) {
                    TextButton(
                        onClick = { vm.cartItems.clear() },
                        colors = ButtonDefaults.textButtonColors(contentColor = CriticalRed)
                    ) {
                        Icon(Icons.Default.DeleteSweep, "Hapus", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hapus Semua")
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // Empty Cart State
            if (vm.cartItems.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp),
                            tint = OnSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text("Keranjang kosong", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                    }
                }
            } else {
                // Cart Items
                LazyColumn(Modifier.weight(1f).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.cartItems, key = { it.productId }) { item ->
                        CartItemCard(
                            item = item,
                            onPriceEdit = { newPrice -> vm.updatePrice(item.productId, newPrice) },
                            onIncrement = { vm.updateQuantity(item.productId, 1) },
                            onDecrement = { vm.updateQuantity(item.productId, -1) },
                            onRemove = { vm.removeFromCart(item.productId) }
                        )
                    }
                }

                // Footer
                Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Pesanan (${vm.totalItems} item)", style = MaterialTheme.typography.bodyLarge)
                        if (vm.totalDiscount > 0) {
                            Surface(color = DiscountPink, shape = RoundedCornerShape(8.dp)) {
                                Text("Diskon ${rp.format(vm.totalDiscount)}", color = DiscountText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    Text(rp.format(vm.totalAmount), style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Pilih Pembayaran", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, "Next")
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            totalAmount = vm.totalAmount,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { paidAmount, method ->
                showPaymentDialog = false
                vm.checkout(paidAmount, method) { result ->
                    navController.navigate("success/${result.transactionId}")
                }
            }
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem, onPriceEdit: (Double) -> Unit,
    onIncrement: () -> Unit, onDecrement: () -> Unit, onRemove: () -> Unit
) {
    var editingPrice by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf(item.sellingPrice.toLong().toString()) }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(item.productName, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                // Delete item button
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Hapus", tint = CriticalRed, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.sellingPrice < item.defaultPrice) {
                    Text(rp.format(item.defaultPrice), style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.LineThrough, color = OnSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                }
                if (editingPrice) {
                    OutlinedTextField(
                        value = priceText, onValueChange = { priceText = it },
                        modifier = Modifier.width(140.dp).height(48.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, trailingIcon = {
                            IconButton(onClick = {
                                priceText.toDoubleOrNull()?.let { onPriceEdit(it) }
                                editingPrice = false
                            }) { Icon(Icons.Default.Check, "OK", tint = Primary) }
                        }
                    )
                } else {
                    Surface(color = SageLight, shape = RoundedCornerShape(8.dp),
                        onClick = { editingPrice = true }) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(rp.format(item.sellingPrice), color = OnPrimaryContainer,
                                fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(14.dp), tint = OnPrimaryContainer)
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Subtotal: ${rp.format(item.subtotal)}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(SurfaceContainerHigh, RoundedCornerShape(20.dp)).padding(horizontal = 4.dp)) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Remove, "Kurang", modifier = Modifier.size(18.dp))
                    }
                    Text("${item.quantity}", modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Add, "Tambah", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDialog(totalAmount: Double, onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var paidText by remember { mutableStateOf(totalAmount.toLong().toString()) }
    var method by remember { mutableStateOf("CASH") }
    var showQrisPopup by remember { mutableStateOf(false) }

    if (showQrisPopup) {
        Dialog(onDismissRequest = { showQrisPopup = false }) {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Scan QRIS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.qris_e_farhan),
                        contentDescription = "QRIS",
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showQrisPopup = false },
                            Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed)
                        ) { Text("Transaksi Gagal") }
                        Button(
                            onClick = {
                                showQrisPopup = false
                                val paidAmount = paidText.toDoubleOrNull() ?: 0.0
                                onConfirm(paidAmount, method)
                            },
                            Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text("Berhasil", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Total: ${rp.format(totalAmount)}", style = MaterialTheme.typography.headlineMedium,
                    color = Primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Payment method
                Text("Metode Bayar", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASH", "QRIS", "TRANSFER").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m },
                            label = { Text(m) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryContainer,
                                selectedLabelColor = OnPrimaryContainer
                            ))
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Paid amount
                Text("Jumlah Bayar", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = paidText, onValueChange = { paidText = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") }, shape = RoundedCornerShape(12.dp)
                )

                // Quick amount buttons
                Spacer(Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val quickAmounts = listOf(
                        totalAmount.toLong(),
                        ((totalAmount / 10000).toLong() + 1) * 10000,
                        ((totalAmount / 50000).toLong() + 1) * 50000,
                        ((totalAmount / 100000).toLong() + 1) * 100000
                    ).distinct().take(4)
                    quickAmounts.forEach { amount ->
                        SuggestionChip(
                            onClick = { paidText = amount.toString() },
                            label = { Text(rp.format(amount), style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                val paidAmount = paidText.toDoubleOrNull() ?: 0.0
                if (paidAmount >= totalAmount) {
                    Text("Kembalian: ${rp.format(paidAmount - totalAmount)}",
                        color = Primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                    Button(
                        onClick = {
                            if (method == "QRIS") {
                                showQrisPopup = true
                            } else {
                                onConfirm(paidAmount, method)
                            }
                        },
                        Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp),
                        enabled = paidAmount >= totalAmount,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("Bayar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
