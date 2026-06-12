package com.tokofarhan.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String?,
    val satuanBeli: String,
    val satuanJual: String,
    val rasioKonversi: Int,
    val currentStokJual: Double,
    val hppMovingAverage: Double,
    val hargaJualDefault: Double,
    val minStockAlert: Int,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "stock_ins")
data class StockInEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val qtyBeli: Double,
    val qtyJualConverted: Double,
    val totalHargaBeli: Double,
    val hppPerUnitJual: Double,
    val hppAfterRestock: Double,
    val timestamp: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val receiptNumber: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: String,
    val timestamp: String
)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val productId: String,
    val productName: String,
    val qtyJual: Double,
    val sellingPrice: Double,
    val recordedHpp: Double,
    val subtotal: Double,
    val profit: Double
)
