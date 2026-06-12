package com.tokofarhan.pos.ui.screens.stock

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tokofarhan.pos.ui.theme.*
import com.tokofarhan.pos.viewmodel.StockViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.*

private val rp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(vm: StockViewModel = viewModel()) {
    val context = LocalContext.current

    // Refresh on recompose
    LaunchedEffect(Unit) { vm.loadProducts() }

    // Observe result message
    val resultMessage = vm.resultMessage.value
    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.resultMessage.value = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(Background)) {
            CenterAlignedTopAppBar(
                title = { Text("Manajemen Stok", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.loadProducts() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
            TabRow(selectedTabIndex = vm.activeTab.value, containerColor = Background) {
                Tab(selected = vm.activeTab.value == 0, onClick = { vm.activeTab.value = 0 }, text = { Text("Daftar Barang") })
                Tab(selected = vm.activeTab.value == 1, onClick = { vm.activeTab.value = 1 }, text = { Text("Beli Stok Masuk") })
            }
            when (vm.activeTab.value) {
                0 -> ProductListTab(vm)
                1 -> RestockTab(vm)
            }
        }

        // FAB for adding new product — only visible in product list tab
        if (vm.activeTab.value == 0) {
            FloatingActionButton(
                onClick = {
                    vm.newProductName.value = ""
                    vm.showAddProductDialog.value = true
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, "Tambah Barang")
            }
        }

        // Dialog for adding a new product
        if (vm.showAddProductDialog.value) {
            AddProductDialog(vm)
        }

        // Delete confirmation dialog
        if (vm.showDeleteConfirm.value) {
            val product = vm.productToDelete.value
            if (product != null) {
                AlertDialog(
                    onDismissRequest = { vm.dismissDeleteConfirm() },
                    icon = { Icon(Icons.Default.Warning, null, tint = CriticalRed) },
                    title = { Text("Hapus Barang?") },
                    text = {
                        Text("Yakin ingin menghapus \"${product.name}\"? Data stok dan harga barang ini akan hilang permanen.")
                    },
                    confirmButton = {
                        Button(
                            onClick = { vm.deleteProduct() },
                            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                        ) { Text("Hapus") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { vm.dismissDeleteConfirm() }) { Text("Batal") }
                    }
                )
            }
        }
        // Edit price dialog
        if (vm.showEditPriceDialog.value) {
            val product = vm.editingProduct.value
            if (product != null) {
                Dialog(onDismissRequest = { vm.dismissEditPrice() }) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Edit Harga Jual", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(product.name, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = vm.editPriceText.value,
                                onValueChange = { vm.editPriceText.value = it },
                                label = { Text("Harga Baru (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { vm.dismissEditPrice() },
                                    Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Batal") }
                                Button(
                                    onClick = { vm.saveEditedPrice() },
                                    Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) { Text("Simpan", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListTab(vm: StockViewModel) {
    Column(Modifier.fillMaxSize()) {
        // Search bar for product list
        OutlinedTextField(
            value = vm.productListSearchQuery.value,
            onValueChange = { vm.productListSearchQuery.value = it },
            placeholder = { Text("Cari barang...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (vm.productListSearchQuery.value.isNotBlank()) {
                    IconButton(onClick = { vm.productListSearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainer,
                unfocusedContainerColor = SurfaceContainer,
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        val products = vm.filteredProductList

        if (products.isEmpty()) {
            // Empty state
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (vm.productListSearchQuery.value.isNotBlank()) "Barang tidak ditemukan"
                        else "Belum ada barang",
                        style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant
                    )
                    if (vm.productListSearchQuery.value.isBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Tekan + untuk menambah barang baru",
                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        } else {
            // Product count badge
            Text(
                "${products.size} barang",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant
            )
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { p ->
                    val isLowStock = p.currentStokJual <= p.minStockAlert && p.minStockAlert > 0
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).background(
                                    if (isLowStock) ErrorContainer else SurfaceContainerHigh,
                                    RoundedCornerShape(12.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Inventory2, null,
                                    tint = if (isLowStock) CriticalRed else PrimaryContainer)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Stok: ${p.currentStokJual.toInt()} ${p.satuanJual}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isLowStock) CriticalRed else OnSurfaceVariant
                                    )
                                    if (isLowStock) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(color = ErrorContainer, shape = RoundedCornerShape(4.dp)) {
                                            Text("Kritis", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall, color = CriticalRed)
                                        }
                                    }
                                }
                                if (p.barcode != null) {
                                    Text("Barcode: ${p.barcode}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(rp.format(p.hargaJualDefault), color = Primary, style = MaterialTheme.typography.labelLarge)
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { vm.openEditPrice(p) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Edit, "Edit Harga", modifier = Modifier.size(16.dp), tint = Primary)
                                    }
                                }
                                Text("HPP: ${rp.format(p.hppMovingAverage)}", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                // Delete button
                                IconButton(onClick = { vm.confirmDeleteProduct(p) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, "Hapus", modifier = Modifier.size(18.dp), tint = CriticalRed.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestockTab(vm: StockViewModel) {
    val context = LocalContext.current

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Pilih Barang", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = vm.searchQuery.value, onValueChange = { vm.searchQuery.value = it },
                        placeholder = { Text("Cari nama barang...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (vm.searchQuery.value.isNotBlank()) {
                                IconButton(onClick = { vm.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                    vm.selectedProduct.value?.let { sel ->
                        Spacer(Modifier.height(12.dp))
                        Surface(color = SurfaceContainerHigh, shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory2, null, tint = Tertiary)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(sel.name, fontWeight = FontWeight.SemiBold)
                                    Text("Stok: ${sel.currentStokJual.toInt()} ${sel.satuanJual}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                                }
                                IconButton(onClick = { vm.clearSelection() }) { Icon(Icons.Default.Close, null, tint = CriticalRed) }
                            }
                        }
                    } ?: run {
                        if (vm.searchQuery.value.isNotBlank()) {
                            val results = vm.filteredProducts.take(5)
                            if (results.isEmpty()) {
                                Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Barang tidak ditemukan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            vm.newProductName.value = vm.searchQuery.value
                                            vm.showAddProductDialog.value = true
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Tambah Barang Baru")
                                    }
                                }
                            } else {
                                results.forEach { p ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        onClick = { vm.selectProduct(p); vm.searchQuery.value = "" }
                                    ) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(20.dp), tint = OnSurfaceVariant)
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(p.name, style = MaterialTheme.typography.bodyLarge)
                                                Text("Stok: ${p.currentStokJual.toInt()} ${p.satuanJual}",
                                                    style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Jumlah Beli", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = vm.qtyBeli.value, onValueChange = { vm.qtyBeli.value = it },
                            modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp), placeholder = { Text("0") })
                        Surface(color = SurfaceContainerHigh, shape = RoundedCornerShape(12.dp)) {
                            Text(vm.selectedProduct.value?.satuanBeli ?: "Unit", Modifier.padding(horizontal = 16.dp, vertical = 14.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    // Show conversion info
                    vm.selectedProduct.value?.let { sel ->
                        val qty = vm.qtyBeli.value.toDoubleOrNull()
                        if (qty != null && qty > 0 && sel.rasioKonversi > 1) {
                            Spacer(Modifier.height(8.dp))
                            Text("= ${(qty * sel.rasioKonversi).toInt()} ${sel.satuanJual}",
                                style = MaterialTheme.typography.bodyMedium, color = Primary)
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Harga Modal", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = vm.totalHarga.value, onValueChange = { vm.totalHarga.value = it },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") }, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Modal Satuan: ${vm.modalSatuan} / ${vm.selectedProduct.value?.satuanBeli ?: "Unit"}",
                        style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    // Show per-unit jual price
                    vm.selectedProduct.value?.let { sel ->
                        val qty = vm.qtyBeli.value.toDoubleOrNull()
                        val total = vm.totalHarga.value.toDoubleOrNull()
                        if (qty != null && total != null && qty > 0 && sel.rasioKonversi > 0) {
                            val hppPerJual = total / (qty * sel.rasioKonversi)
                            Text("HPP per ${sel.satuanJual}: Rp ${"%,.0f".format(hppPerJual)}",
                                style = MaterialTheme.typography.bodyMedium, color = Primary)
                        }
                    }
                }
            }
        }
        item {
            Surface(color = BlueLight, shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    Icon(Icons.Default.Info, null, tint = Secondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Sistem otomatis mengonversi ke satuan eceran dan menyesuaikan rata-rata modal.",
                        style = MaterialTheme.typography.bodyMedium, color = Secondary)
                }
            }
        }
        item {
            // Button is always enabled - validation happens on click with user feedback
            Button(
                onClick = {
                    when {
                        vm.selectedProduct.value == null ->
                            Toast.makeText(context, "Pilih barang terlebih dahulu!", Toast.LENGTH_SHORT).show()
                        vm.qtyBeli.value.isBlank() || (vm.qtyBeli.value.toDoubleOrNull() ?: 0.0) <= 0 ->
                            Toast.makeText(context, "Masukkan jumlah beli yang valid!", Toast.LENGTH_SHORT).show()
                        vm.totalHarga.value.isBlank() || (vm.totalHarga.value.toDoubleOrNull() ?: 0.0) <= 0 ->
                            Toast.makeText(context, "Masukkan total harga modal yang valid!", Toast.LENGTH_SHORT).show()
                        else -> vm.submitRestock()
                    }
                },
                Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Simpan Stok Baru", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun AddProductDialog(vm: StockViewModel) {
    Dialog(
        onDismissRequest = { vm.showAddProductDialog.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Tambah Barang Baru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = vm.newProductName.value,
                    onValueChange = { vm.newProductName.value = it },
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = vm.newProductBarcode.value,
                    onValueChange = { vm.newProductBarcode.value = it },
                    label = { Text("Barcode (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = vm.newProductHargaJual.value,
                    onValueChange = { vm.newProductHargaJual.value = it },
                    label = { Text("Harga Jual (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = vm.newProductSatuanBeli.value,
                        onValueChange = { vm.newProductSatuanBeli.value = it },
                        label = { Text("Satuan Beli") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = vm.newProductSatuanJual.value,
                        onValueChange = { vm.newProductSatuanJual.value = it },
                        label = { Text("Satuan Jual") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = vm.newProductRasioKonversi.value,
                        onValueChange = { vm.newProductRasioKonversi.value = it },
                        label = { Text("Rasio Konversi") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = vm.newProductMinStockAlert.value,
                        onValueChange = { vm.newProductMinStockAlert.value = it },
                        label = { Text("Min. Stok Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { vm.showAddProductDialog.value = false },
                        Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Batal") }
                    Button(
                        onClick = { vm.saveNewProduct() },
                        Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("Simpan", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
