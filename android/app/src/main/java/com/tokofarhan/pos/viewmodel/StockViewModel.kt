package com.tokofarhan.pos.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tokofarhan.pos.TokoFarhanApp
import com.tokofarhan.pos.data.local.entity.ProductEntity
import com.tokofarhan.pos.data.repository.ProductRepository
import kotlinx.coroutines.launch

class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TokoFarhanApp).database
    private val repo = ProductRepository(db.productDao())
    
    var products = mutableStateOf<List<ProductEntity>>(emptyList())
    var selectedProduct = mutableStateOf<ProductEntity?>(null)
    var searchQuery = mutableStateOf("")
    var qtyBeli = mutableStateOf("")
    var totalHarga = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var resultMessage = mutableStateOf<String?>(null)
    var activeTab = mutableStateOf(0)

    // Add New Product States
    var showAddProductDialog = mutableStateOf(false)
    var newProductName = mutableStateOf("")
    var newProductBarcode = mutableStateOf("")
    var newProductSatuanBeli = mutableStateOf("Dus")
    var newProductSatuanJual = mutableStateOf("Pcs")
    var newProductRasioKonversi = mutableStateOf("1")
    var newProductHargaJual = mutableStateOf("")
    var newProductMinStockAlert = mutableStateOf("5")

    // Delete confirmation
    var showDeleteConfirm = mutableStateOf(false)
    var productToDelete = mutableStateOf<ProductEntity?>(null)

    // Edit price
    var showEditPriceDialog = mutableStateOf(false)
    var editingProduct = mutableStateOf<ProductEntity?>(null)
    var editPriceText = mutableStateOf("")

    // Search in product list tab
    var productListSearchQuery = mutableStateOf("")

    val filteredProducts: List<ProductEntity>
        get() {
            val q = searchQuery.value.lowercase()
            return if (q.isBlank()) products.value
            else products.value.filter { it.name.lowercase().contains(q) }
        }

    val filteredProductList: List<ProductEntity>
        get() {
            val q = productListSearchQuery.value.lowercase()
            return if (q.isBlank()) products.value
            else products.value.filter {
                it.name.lowercase().contains(q) || (it.barcode?.lowercase()?.contains(q) == true)
            }
        }

    val modalSatuan: String
        get() {
            val qty = qtyBeli.value.toDoubleOrNull() ?: return "-"
            val total = totalHarga.value.toDoubleOrNull() ?: return "-"
            if (qty <= 0) return "-"
            val pricePerUnit = total / qty
            return "Rp ${"%,.0f".format(pricePerUnit)}"
        }

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            isLoading.value = true
            try { products.value = repo.getProducts() }
            catch (_: Exception) {}
            finally { isLoading.value = false }
        }
    }

    fun selectProduct(product: ProductEntity) { selectedProduct.value = product }
    fun clearSelection() { selectedProduct.value = null }

    fun confirmDeleteProduct(product: ProductEntity) {
        productToDelete.value = product
        showDeleteConfirm.value = true
    }

    fun dismissDeleteConfirm() {
        showDeleteConfirm.value = false
        productToDelete.value = null
    }

    fun deleteProduct() {
        val product = productToDelete.value ?: return
        viewModelScope.launch {
            isLoading.value = true
            try {
                repo.deleteProduct(product.id)
                resultMessage.value = "Barang \"${product.name}\" berhasil dihapus"
                showDeleteConfirm.value = false
                productToDelete.value = null
                loadProducts()
            } catch (e: Exception) {
                resultMessage.value = "Gagal menghapus barang: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun openEditPrice(product: ProductEntity) {
        editingProduct.value = product
        editPriceText.value = product.hargaJualDefault.toLong().toString()
        showEditPriceDialog.value = true
    }

    fun dismissEditPrice() {
        showEditPriceDialog.value = false
        editingProduct.value = null
    }

    fun saveEditedPrice() {
        val product = editingProduct.value ?: return
        val newPrice = editPriceText.value.toDoubleOrNull()
        if (newPrice == null || newPrice <= 0) {
            resultMessage.value = "Masukkan harga yang valid!"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            try {
                repo.updateProductPrice(product.id, newPrice)
                resultMessage.value = "Harga \"${product.name}\" diperbarui ke Rp ${"%,.0f".format(newPrice)}"
                showEditPriceDialog.value = false
                editingProduct.value = null
                loadProducts()
            } catch (e: Exception) {
                resultMessage.value = "Gagal mengubah harga: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun submitRestock() {
        val product = selectedProduct.value ?: return
        val qty = qtyBeli.value.toDoubleOrNull() ?: return
        val total = totalHarga.value.toDoubleOrNull() ?: return
        if (qty <= 0 || total <= 0) return

        viewModelScope.launch {
            isLoading.value = true
            try {
                val success = repo.restockProduct(product.id, qty, total)
                if (success) {
                    resultMessage.value = "Stok berhasil ditambahkan"
                    qtyBeli.value = ""
                    totalHarga.value = ""
                    selectedProduct.value = null
                    loadProducts()
                } else { resultMessage.value = "Gagal menambah stok" }
            } catch (e: Exception) { resultMessage.value = "Error: ${e.message}" }
            finally { isLoading.value = false }
        }
    }

    fun saveNewProduct() {
        val name = newProductName.value.trim()
        val satuanBeli = newProductSatuanBeli.value.trim()
        val satuanJual = newProductSatuanJual.value.trim()
        val rasio = newProductRasioKonversi.value.toIntOrNull() ?: 1
        val hargaJual = newProductHargaJual.value.toDoubleOrNull() ?: 0.0
        val minStock = newProductMinStockAlert.value.toIntOrNull() ?: 0
        val barcode = newProductBarcode.value.trim().takeIf { it.isNotBlank() }

        if (name.isEmpty() || hargaJual <= 0) {
            resultMessage.value = "Nama barang dan harga jual harus diisi valid!"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                val newProduct = ProductEntity(
                    name = name,
                    barcode = barcode,
                    satuanBeli = satuanBeli,
                    satuanJual = satuanJual,
                    rasioKonversi = rasio,
                    currentStokJual = 0.0,
                    hppMovingAverage = 0.0,
                    hargaJualDefault = hargaJual,
                    minStockAlert = minStock,
                    createdAt = now,
                    updatedAt = now
                )
                repo.addProduct(newProduct)
                resultMessage.value = "Barang baru berhasil ditambahkan"
                showAddProductDialog.value = false
                
                // Clear fields
                newProductName.value = ""
                newProductBarcode.value = ""
                newProductHargaJual.value = ""
                
                // Reload and select
                products.value = repo.getProducts()
                val newlyAdded = products.value.find { it.name == name }
                if (newlyAdded != null) {
                    selectProduct(newlyAdded)
                    searchQuery.value = ""
                }
            } catch (e: Exception) {
                resultMessage.value = "Gagal menambah barang: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
