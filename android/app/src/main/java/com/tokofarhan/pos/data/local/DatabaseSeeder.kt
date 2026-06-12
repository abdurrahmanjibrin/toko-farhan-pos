package com.tokofarhan.pos.data.local

import android.content.Context
import android.util.Log
import com.tokofarhan.pos.data.local.dao.ProductDao
import com.tokofarhan.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

object DatabaseSeeder {
    suspend fun seedDatabase(context: Context, productDao: ProductDao) {
        withContext(Dispatchers.IO) {
            try {
                // Check if database is already populated
                if (productDao.getAllProducts().isNotEmpty()) {
                    Log.d("DatabaseSeeder", "Database is already populated. Skipping seed.")
                    return@withContext
                }

                Log.d("DatabaseSeeder", "Starting to seed database from CSV...")
                val inputStream = context.assets.open("products.csv")
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                var isFirstLine = true
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val now = sdf.format(Date())
                val digitRegex = Regex("\\d+")

                for (line in reader.lineSequence()) {
                    if (isFirstLine) {
                        isFirstLine = false
                        continue // Skip header
                    }

                    // Simple CSV split (assuming no commas inside quotes)
                    val cols = line.split(",")
                    if (cols.size >= 7) {
                        val name = "${cols[1].trim()} ${cols[2].trim()}"
                        val hargaKulak = cols[3].trim().toDoubleOrNull() ?: 0.0
                        val hargaJual = cols[4].trim().toDoubleOrNull() ?: 0.0
                        
                        val stokAmanStr = cols[5]
                        val minRestockStr = cols[6]
                        
                        val currentStokJual = digitRegex.find(stokAmanStr)?.value?.toDoubleOrNull() ?: 0.0
                        val minStockAlert = digitRegex.find(minRestockStr)?.value?.toIntOrNull() ?: 0

                        val product = ProductEntity(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            barcode = null,
                            satuanBeli = "Unit",
                            satuanJual = "Unit",
                            rasioKonversi = 1,
                            currentStokJual = currentStokJual,
                            hppMovingAverage = hargaKulak,
                            hargaJualDefault = hargaJual,
                            minStockAlert = minStockAlert,
                            createdAt = now,
                            updatedAt = now
                        )
                        
                        productDao.insertProduct(product)
                    }
                }
                
                reader.close()
                Log.d("DatabaseSeeder", "Database seeded successfully!")
            } catch (e: Exception) {
                Log.e("DatabaseSeeder", "Error seeding database: ${e.message}")
            }
        }
    }
}
