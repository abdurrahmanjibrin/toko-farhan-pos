package com.tokofarhan.pos.data.local.dao

import androidx.room.*
import com.tokofarhan.pos.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE currentStokJual <= minStockAlert AND minStockAlert > 0 ORDER BY currentStokJual ASC")
    suspend fun getLowStockProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query("UPDATE products SET hargaJualDefault = :newPrice, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProductPrice(id: String, newPrice: Double, updatedAt: String)

    @Insert
    suspend fun insertStockIn(stockIn: StockInEntity)
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactions(limit: Int = 50, offset: Int = 0): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItems(transactionId: String): List<TransactionItemEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE DATE(timestamp) = DATE('now', 'localtime')")
    suspend fun getTransactionCountToday(): Int

    @Query("SELECT * FROM transactions WHERE DATE(timestamp) = :date ORDER BY timestamp DESC")
    suspend fun getTransactionsByDate(date: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY timestamp ASC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("DELETE FROM transaction_items WHERE transactionId IN (SELECT id FROM transactions WHERE DATE(timestamp) >= :startDate)")
    suspend fun deleteTransactionItemsFromDate(startDate: String)

    @Query("DELETE FROM transactions WHERE DATE(timestamp) >= :startDate")
    suspend fun deleteTransactionsFromDate(startDate: String)
}

data class ProfitLossEntry(
    val date: String,
    val totalTransactions: Int,
    val totalRevenue: Double,
    val totalHppCost: Double,
    val totalProfit: Double
)

@Dao
interface ReportDao {
    // SQLite DATE() function to group by date
    @Query("""
        SELECT 
            DATE(t.timestamp) as date,
            COUNT(DISTINCT t.id) as totalTransactions,
            SUM(t.totalAmount) as totalRevenue,
            SUM(ti.qtyJual * ti.recordedHpp) as totalHppCost,
            SUM(ti.profit) as totalProfit
        FROM transactions t
        JOIN transaction_items ti ON t.id = ti.transactionId
        GROUP BY DATE(t.timestamp)
        ORDER BY date ASC
    """)
    suspend fun getDailyProfitLoss(): List<ProfitLossEntry>
    
    @Query("SELECT COUNT(*) FROM products WHERE currentStokJual <= minStockAlert AND minStockAlert > 0")
    suspend fun getLowStockCount(): Int
}
