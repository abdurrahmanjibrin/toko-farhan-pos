package com.tokofarhan.pos.data.model

data class CartItem(
    val productId: String,
    val productName: String,
    val satuanJual: String,
    val defaultPrice: Double,
    var sellingPrice: Double,
    var quantity: Int = 1,
    val currentStock: Double
) {
    val subtotal: Double get() = sellingPrice * quantity
    val discount: Double get() = if (sellingPrice < defaultPrice) (defaultPrice - sellingPrice) * quantity else 0.0
}
