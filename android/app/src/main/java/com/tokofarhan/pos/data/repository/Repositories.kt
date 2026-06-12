package com.tokofarhan.pos.data.repository

import com.tokofarhan.pos.data.local.dao.ProductDao
import com.tokofarhan.pos.data.local.dao.ReportDao
import com.tokofarhan.pos.data.local.dao.TransactionDao
import com.tokofarhan.pos.data.local.entity.ProductEntity
import com.tokofarhan.pos.data.local.entity.StockInEntity
import com.tokofarhan.pos.data.local.entity.TransactionEntity
import com.tokofarhan.pos.data.local.entity.TransactionItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProductRepository(private val productDao: ProductDao) {
    suspend fun getProducts(): List<ProductEntity> = productDao.getAllProducts()
    suspend fun getLowStockProducts(): List<ProductEntity> = productDao.getLowStockProducts()

    suspend fun addProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }

    suspend fun updateProductPrice(productId: String, newPrice: Double) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        productDao.updateProductPrice(productId, newPrice, now)
    }

    suspend fun restockProduct(productId: String, qtyBeli: Double, totalHarga: Double): Boolean {
        val product = productDao.getProductById(productId) ?: return false

        val qtyJualConverted = qtyBeli * product.rasioKonversi
        val hppPerUnitJual = totalHarga / qtyJualConverted

        // Calculate Moving Average HPP
        // ((OldStock * OldHPP) + (NewStock * NewHPP)) / (OldStock + NewStock)
        val oldTotalValue = product.currentStokJual * product.hppMovingAverage
        val newTotalValue = oldTotalValue + totalHarga
        val newTotalStock = product.currentStokJual + qtyJualConverted

        val hppAfterRestock = if (newTotalStock > 0) newTotalValue / newTotalStock else 0.0

        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        val stockIn = StockInEntity(
            productId = productId,
            qtyBeli = qtyBeli,
            qtyJualConverted = qtyJualConverted,
            totalHargaBeli = totalHarga,
            hppPerUnitJual = hppPerUnitJual,
            hppAfterRestock = hppAfterRestock,
            timestamp = now
        )

        productDao.insertStockIn(stockIn)

        val updatedProduct = product.copy(
            currentStokJual = newTotalStock,
            hppMovingAverage = hppAfterRestock,
            updatedAt = now
        )
        productDao.updateProduct(updatedProduct)

        return true
    }
}

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val productDao: ProductDao
) {
    data class SaleItemRequest(
        val productId: String,
        val qtyJual: Double,
        val sellingPrice: Double
    )

    suspend fun createTransaction(
        items: List<SaleItemRequest>,
        paidAmount: Double,
        paymentMethod: String
    ): TransactionEntity? {
        if (items.isEmpty()) return null

        var totalAmount = 0.0
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        // Generate receipt number e.g. TF-20231024-0001
        val todayCount = transactionDao.getTransactionCountToday() + 1
        val receiptNumber = "TF-$dateCode-${todayCount.toString().padStart(4, '0')}"

        val transactionId = UUID.randomUUID().toString()
        val transactionItems = mutableListOf<TransactionItemEntity>()

        for (item in items) {
            val product = productDao.getProductById(item.productId) ?: return null
            if (product.currentStokJual < item.qtyJual) return null // Out of stock

            val subtotal = item.sellingPrice * item.qtyJual
            val profit = (item.sellingPrice - product.hppMovingAverage) * item.qtyJual
            totalAmount += subtotal

            transactionItems.add(
                TransactionItemEntity(
                    transactionId = transactionId,
                    productId = product.id,
                    productName = product.name,
                    qtyJual = item.qtyJual,
                    sellingPrice = item.sellingPrice,
                    recordedHpp = product.hppMovingAverage,
                    subtotal = subtotal,
                    profit = profit
                )
            )

            // Deduct stock
            val updatedProduct = product.copy(
                currentStokJual = product.currentStokJual - item.qtyJual,
                updatedAt = now
            )
            productDao.updateProduct(updatedProduct)
        }

        val changeAmount = paidAmount - totalAmount
        if (changeAmount < 0) return null // Insufficient payment

        val transaction = TransactionEntity(
            id = transactionId,
            receiptNumber = receiptNumber,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod,
            timestamp = now
        )

        transactionDao.insertTransaction(transaction)
        transactionDao.insertTransactionItems(transactionItems)

        return transaction
    }

    suspend fun getTransactions() = transactionDao.getTransactions()

    suspend fun getTransactionsByDate(date: String) = transactionDao.getTransactionsByDate(date)

    suspend fun getTransactionDetail(id: String): Pair<TransactionEntity, List<TransactionItemEntity>>? {
        val tx = transactionDao.getTransactionById(id) ?: return null
        val items = transactionDao.getTransactionItems(id)
        return Pair(tx, items)
    }

    suspend fun getAllTransactionsWithItems(): List<Pair<TransactionEntity, List<TransactionItemEntity>>> {
        val allTx = transactionDao.getAllTransactions()
        return allTx.map { tx ->
            val items = transactionDao.getTransactionItems(tx.id)
            Pair(tx, items)
        }
    }
}

class ReportRepository(private val reportDao: ReportDao) {
    suspend fun getDailyProfitLoss() = reportDao.getDailyProfitLoss()
    suspend fun getLowStockCount() = reportDao.getLowStockCount()
}
