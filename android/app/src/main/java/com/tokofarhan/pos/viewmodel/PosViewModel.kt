package com.tokofarhan.pos.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tokofarhan.pos.TokoFarhanApp
import com.tokofarhan.pos.data.local.entity.ProductEntity
import com.tokofarhan.pos.data.model.CartItem
import com.tokofarhan.pos.data.repository.ProductRepository
import com.tokofarhan.pos.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TokoFarhanApp).database
    private val productRepo = ProductRepository(db.productDao())
    private val transactionRepo = TransactionRepository(db.transactionDao(), db.productDao())

    var products = mutableStateOf<List<ProductEntity>>(emptyList())
    var isLoading = mutableStateOf(false)
    var searchQuery = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)
    val cartItems = mutableStateListOf<CartItem>()
    var showCart = mutableStateOf(false)
    var checkoutResult = mutableStateOf<CheckoutResult?>(null)

    data class CheckoutResult(
        val transactionId: String, val receiptNumber: String, val totalAmount: Double,
        val paidAmount: Double, val changeAmount: Double
    )

    val filteredProducts: List<ProductEntity>
        get() {
            val q = searchQuery.value.lowercase()
            return if (q.isBlank()) products.value
            else products.value.filter {
                it.name.lowercase().contains(q) || (it.barcode?.contains(q) == true)
            }
        }

    val lowStockProducts: List<ProductEntity>
        get() = products.value.filter { it.currentStokJual <= it.minStockAlert && it.minStockAlert > 0 }

    var showLowStockDialog = mutableStateOf(false)

    val totalItems: Int get() = cartItems.sumOf { it.quantity }
    val totalAmount: Double get() = cartItems.sumOf { it.subtotal }
    val totalDiscount: Double get() = cartItems.sumOf { it.discount }

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try { products.value = productRepo.getProducts() }
            catch (e: Exception) { errorMessage.value = "Gagal memuat produk: ${e.message}" }
            finally { isLoading.value = false }
        }
    }

    fun addToCart(product: ProductEntity) {
        val existing = cartItems.find { it.productId == product.id }
        if (existing != null) {
            val idx = cartItems.indexOf(existing)
            if (existing.quantity < existing.currentStock.toInt()) {
                cartItems[idx] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            cartItems.add(CartItem(
                productId = product.id, productName = product.name,
                satuanJual = product.satuanJual, defaultPrice = product.hargaJualDefault,
                sellingPrice = product.hargaJualDefault, quantity = 1,
                currentStock = product.currentStokJual
            ))
        }
    }

    fun updateQuantity(productId: String, delta: Int) {
        val idx = cartItems.indexOfFirst { it.productId == productId }
        if (idx >= 0) {
            val item = cartItems[idx]
            val newQty = item.quantity + delta
            if (newQty <= 0) cartItems.removeAt(idx)
            else if (newQty <= item.currentStock.toInt()) cartItems[idx] = item.copy(quantity = newQty)
        }
    }

    fun updatePrice(productId: String, newPrice: Double) {
        val idx = cartItems.indexOfFirst { it.productId == productId }
        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(sellingPrice = newPrice)
    }

    fun removeFromCart(productId: String) {
        cartItems.removeAll { it.productId == productId }
    }

    fun getCartQuantity(productId: String): Int =
        cartItems.find { it.productId == productId }?.quantity ?: 0

    fun checkout(paidAmount: Double, paymentMethod: String, onSuccess: (CheckoutResult) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val saleLines = cartItems.map {
                    TransactionRepository.SaleItemRequest(it.productId, it.quantity.toDouble(), it.sellingPrice)
                }
                val tx = transactionRepo.createTransaction(saleLines, paidAmount, paymentMethod)
                if (tx != null) {
                    val result = CheckoutResult(tx.id, tx.receiptNumber, tx.totalAmount, tx.paidAmount, tx.changeAmount)
                    checkoutResult.value = result
                    cartItems.clear()
                    showCart.value = false
                    loadProducts()
                    onSuccess(result)
                } else { errorMessage.value = "Checkout gagal (Stok tidak cukup atau pembayaran kurang)" }
            } catch (e: Exception) { errorMessage.value = "Checkout error: ${e.message}" }
            finally { isLoading.value = false }
        }
    }
}
