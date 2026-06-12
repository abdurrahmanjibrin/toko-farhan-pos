package com.tokofarhan.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tokofarhan.pos.data.local.dao.*
import com.tokofarhan.pos.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        ProductEntity::class,
        StockInEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tokofarhan_pos_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.productDao())
                }
            }
        }

        suspend fun populateDatabase(productDao: ProductDao) {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Seed some default products so the app isn't empty on first install
            val products = listOf(
                ProductEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Minyak Goreng Bimoli 2L",
                    barcode = "8991234567890",
                    satuanBeli = "Dus",
                    satuanJual = "Pouch",
                    rasioKonversi = 6,
                    currentStokJual = 12.0,
                    hppMovingAverage = 32000.0,
                    hargaJualDefault = 35000.0,
                    minStockAlert = 5,
                    createdAt = now,
                    updatedAt = now
                ),
                ProductEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Beras Maknyuss 5kg",
                    barcode = "8990987654321",
                    satuanBeli = "Karung",
                    satuanJual = "Sak",
                    rasioKonversi = 1,
                    currentStokJual = 8.0,
                    hppMovingAverage = 58000.0,
                    hargaJualDefault = 62000.0,
                    minStockAlert = 2,
                    createdAt = now,
                    updatedAt = now
                )
            )
            products.forEach { productDao.insertProduct(it) }
        }
    }
}
