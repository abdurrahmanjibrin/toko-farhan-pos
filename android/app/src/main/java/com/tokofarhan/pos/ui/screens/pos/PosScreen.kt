package com.tokofarhan.pos.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tokofarhan.pos.data.local.entity.ProductEntity
import com.tokofarhan.pos.ui.screens.cart.CartBottomSheet
import com.tokofarhan.pos.ui.theme.*
import com.tokofarhan.pos.viewmodel.PosViewModel
import java.text.NumberFormat
import java.util.Locale

private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(navController: NavController, vm: PosViewModel = viewModel()) {
    val products = vm.filteredProducts

    // Refresh products when screen is recomposed
    LaunchedEffect(Unit) {
        vm.loadProducts()
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar — removed dead Menu icon, notification now opens cart
            CenterAlignedTopAppBar(
                title = { Text("Toko FARHAN AL-BASYAR", fontWeight = FontWeight.Bold) },
                actions = {
                    val lowStockCount = vm.lowStockProducts.size
                    if (lowStockCount > 0) {
                        BadgedBox(
                            badge = { Badge(containerColor = CriticalRed) { Text("$lowStockCount", color = Color.White) } },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            IconButton(onClick = { vm.showLowStockDialog.value = true }) {
                                Icon(Icons.Default.Notifications, "Notifikasi")
                            }
                        }
                    } else {
                        IconButton(onClick = { vm.showLowStockDialog.value = true }, modifier = Modifier.padding(end = 16.dp)) {
                            Icon(Icons.Default.NotificationsNone, "Notifikasi")
                        }
                    }
                    BadgedBox(badge = { if (vm.totalItems > 0) Badge { Text("${vm.totalItems}") } }) {
                        IconButton(onClick = { if (vm.totalItems > 0) vm.showCart.value = true }) {
                            Icon(Icons.Default.ShoppingCart, "Keranjang")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            // Search Bar
            OutlinedTextField(
                value = vm.searchQuery.value,
                onValueChange = { vm.searchQuery.value = it },
                placeholder = { Text("Cari atau scan barang...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    if (vm.searchQuery.value.isNotBlank()) {
                        IconButton(onClick = { vm.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainer,
                    unfocusedContainerColor = SurfaceContainer,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Loading / Error
            if (vm.isLoading.value) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            vm.errorMessage.value?.let { msg ->
                Text(msg, color = Error, modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodyMedium)
            }

            // Empty state
            if (!vm.isLoading.value && products.isEmpty() && vm.errorMessage.value == null) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(64.dp), tint = OnSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (vm.searchQuery.value.isNotBlank()) "Barang tidak ditemukan" else "Belum ada barang",
                            style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant
                        )
                        if (vm.searchQuery.value.isNotBlank()) {
                            Text("Coba kata kunci lain", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                // Product Grid
                // Product List
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(product = product, cartQty = vm.getCartQuantity(product.id),
                            onAdd = { vm.addToCart(product) },
                            onIncrement = { vm.updateQuantity(product.id, 1) },
                            onDecrement = { vm.updateQuantity(product.id, -1) }
                        )
                    }
                }
            }

            // Bottom Cart Bar
            if (vm.totalItems > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Surface
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Item: ${vm.totalItems}", style = MaterialTheme.typography.bodyLarge)
                            Text(rupiahFormat.format(vm.totalAmount),
                                style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { vm.showCart.value = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text("BAYAR SEKARANG", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        // Cart Bottom Sheet
        if (vm.showCart.value) {
            CartBottomSheet(vm = vm, navController = navController)
        }

        // Low Stock Dialog
        if (vm.showLowStockDialog.value) {
            LowStockDialog(vm = vm)
        }
    }
}

@Composable
private fun LowStockDialog(vm: PosViewModel) {
    val lowStockProducts = vm.lowStockProducts

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
                androidx.compose.foundation.lazy.LazyColumn(
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

@Composable
private fun ProductCard(
    product: ProductEntity, cartQty: Int,
    onAdd: () -> Unit, onIncrement: () -> Unit, onDecrement: () -> Unit
) {
    val isLowStock = product.currentStokJual <= product.minStockAlert && product.minStockAlert > 0

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rupiahFormat.format(product.hargaJualDefault),
                        style = MaterialTheme.typography.labelLarge, color = Primary)
                    Spacer(Modifier.width(8.dp))
                    if (isLowStock) {
                        Text("Stok: ${product.currentStokJual.toInt()} (Kritis)",
                            color = CriticalRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Stok: ${product.currentStokJual.toInt()}",
                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            if (cartQty == 0) {
                FilledIconButton(onClick = onAdd, modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SurfaceContainerHigh)
                ) { Icon(Icons.Default.Add, "Tambah", modifier = Modifier.size(20.dp)) }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(SurfaceContainerHigh, RoundedCornerShape(20.dp)).padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, "Kurang", modifier = Modifier.size(16.dp))
                    }
                    Text("$cartQty", modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, "Tambah", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
