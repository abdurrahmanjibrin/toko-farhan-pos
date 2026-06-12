package com.tokofarhan.pos.data.local

import android.util.Log
import com.tokofarhan.pos.data.local.dao.ProductDao
import com.tokofarhan.pos.data.local.dao.TransactionDao
import com.tokofarhan.pos.data.local.entity.ProductEntity
import com.tokofarhan.pos.data.local.entity.TransactionEntity
import com.tokofarhan.pos.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Generates realistic transaction data for Toko Farhan — a large kelontong store
 * located in a traditional market (pasar). Data spans May 12-18, 2026.
 *
 * Characteristics modeled:
 * - Higher traffic on weekend/market days (Sat-Sun), lower on Monday
 * - ~75% CASH (pasar culture), ~18% QRIS, ~7% TRANSFER
 * - 1-5 items per transaction (weighted toward 2-3)
 * - Transactions spread across store hours 06:00 - 20:00
 * - Some products deliberately driven to low stock / below restock threshold
 */
object TransactionSeeder {

    private val random = Random(42) // deterministic seed for reproducibility

    // Day configs: date -> (targetTxCount, dayLabel)
    private val dayConfigs = listOf(
        "2026-05-12" to 20,  // Selasa
        "2026-05-13" to 25,  // Rabu
        "2026-05-14" to 22,  // Kamis
        "2026-05-15" to 28,  // Jumat
        "2026-05-16" to 35,  // Sabtu
        "2026-05-17" to 30,  // Minggu
        "2026-05-18" to 18,  // Senin
        "2026-05-19" to 20,  // Selasa
        "2026-05-20" to 24,  // Rabu
        "2026-05-21" to 22,  // Kamis
        "2026-05-22" to 28,  // Jumat
        "2026-05-23" to 35,  // Sabtu
        "2026-05-24" to 32,  // Minggu
        "2026-05-25" to 45,  // Senin - H-2 Idul Adha (Spike)
        "2026-05-26" to 55,  // Selasa - H-1 Idul Adha (Spike)
        "2026-05-27" to 35,  // Rabu - Hari Raya Idul Adha (Buka Jam 8-12 Promo)
        "2026-05-28" to 60, // Kamis - Promo
        "2026-05-29" to 70, // Jumat - Promo
        "2026-05-30" to 85, // Sabtu - Promo
        "2026-05-31" to 75  // Minggu - Promo
    )

    // Products that sell frequently in a kelontong (by name substring match)
    // These will be picked more often to simulate realistic sales patterns
    private val hotProductKeywords = listOf(
        "Minyak", "Gula", "Indomie", "Sedaap", "Aqua", "Le Minerale",
        "Sabun", "Rinso", "Rokok", "Sambal", "Kecap", "Teh", "Kopi",
        "Telur", "Tepung", "Mie", "Shampo", "Pasta Gigi", "Detergen",
        "Bawang", "Garam", "Royco", "Masako", "Sasa"
    )

    // Promo products for Eid al-Adha sale
    private val promoKeywords = listOf(
        "Kecap", "Bango", "ABC", "Sedaap", "Sedap", "Kacang"
    )

    // Products to deliberately drain stock on (simulate needing restock)
    private val drainKeywords = listOf(
        "Indomie Goreng", "Aqua Botol", "Beras Premium Sania", "Gulaku",
        "Bimoli", "Rinso", "Minyakita", "Kopi Kapal Api", "Rokok Gudang",
        "Sabun Lifebuoy", "Teh Pucuk", "Le Minerale", "Tepung Segitiga"
    )

    suspend fun seedTransactions(context: android.content.Context, productDao: ProductDao, transactionDao: TransactionDao) {
        withContext(Dispatchers.IO) {
            try {
                // Clear all seeded data so we can regenerate with correct hours
                transactionDao.deleteTransactionItemsFromDate("2026-05-12")
                transactionDao.deleteTransactionsFromDate("2026-05-12")

                // Reset product stocks to original CSV values before re-seeding
                resetProductStocks(context, productDao)

                val allProducts = productDao.getAllProducts().toMutableList()
                if (allProducts.isEmpty()) {
                    Log.e("TransactionSeeder", "No products found! Seed products first.")
                    return@withContext
                }

                Log.d("TransactionSeeder", "Starting transaction seed for ${allProducts.size} products...")

                // Categorize products
                val hotProducts = allProducts.filter { p ->
                    hotProductKeywords.any { kw -> p.name.contains(kw, ignoreCase = true) }
                }
                val promoProducts = allProducts.filter { p ->
                    promoKeywords.any { kw -> p.name.contains(kw, ignoreCase = true) }
                }
                val drainProducts = allProducts.filter { p ->
                    drainKeywords.any { kw -> p.name.contains(kw, ignoreCase = true) }
                }

                // Track stock changes
                val stockUsage = mutableMapOf<String, Double>() // productId -> total qty sold

                var globalReceiptCounter = 0
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                for ((dateStr, txCount) in dayConfigs) {
                    var dailyReceiptNumber = 1

                    // Generate transaction timestamps spread across the day
                    val timestamps = generateTimestamps(dateStr, txCount)

                    for (i in 0 until txCount) {
                        globalReceiptCounter++
                        val txId = UUID.randomUUID().toString()
                        val timestamp = timestamps[i]

                        // Determine number of items (weighted: 1-5, avg ~2.8)
                        val itemCount = weightedItemCount()

                        // Pick products for this transaction
                        val selectedProducts = pickProducts(allProducts, hotProducts, promoProducts, itemCount, dateStr)

                        // Determine payment method
                        val paymentMethod = pickPaymentMethod()

                        // Build transaction items
                        val txItems = mutableListOf<TransactionItemEntity>()
                        var totalAmount = 0.0

                        for (product in selectedProducts) {
                            // Qty: usually 1-3, sometimes more for cheap/bulk items
                            val qty = pickQuantity(product)
                            val sellingPrice = product.hargaJualDefault
                            val hpp = product.hppMovingAverage
                            val subtotal = qty * sellingPrice
                            val profit = qty * (sellingPrice - hpp)

                            txItems.add(
                                TransactionItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    transactionId = txId,
                                    productId = product.id,
                                    productName = product.name,
                                    qtyJual = qty.toDouble(),
                                    sellingPrice = sellingPrice,
                                    recordedHpp = hpp,
                                    subtotal = subtotal,
                                    profit = profit
                                )
                            )
                            totalAmount += subtotal

                            // Track stock usage
                            stockUsage[product.id] = (stockUsage[product.id] ?: 0.0) + qty
                        }

                        // Calculate paid amount (realistic rounding)
                        val paidAmount = calculatePaidAmount(totalAmount, paymentMethod)
                        val changeAmount = paidAmount - totalAmount

                        val receiptNumber = "TF-${dateStr.replace("-", "")}-${String.format("%03d", dailyReceiptNumber)}"
                        dailyReceiptNumber++

                        val transaction = TransactionEntity(
                            id = txId,
                            receiptNumber = receiptNumber,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            changeAmount = if (changeAmount > 0) changeAmount else 0.0,
                            paymentMethod = paymentMethod,
                            timestamp = timestamp
                        )

                        transactionDao.insertTransaction(transaction)
                        transactionDao.insertTransactionItems(txItems)
                    }
                    Log.d("TransactionSeeder", "Seeded $txCount transactions for $dateStr")
                }

                // Now update product stocks based on usage
                // Also deliberately drain some products below their restock threshold
                val now = sdf.format(Date())
                for (product in allProducts) {
                    val totalSold = stockUsage[product.id] ?: 0.0
                    if (totalSold > 0) {
                        var newStock = product.currentStokJual - totalSold

                        // Extra drain for targeted products
                        if (drainProducts.contains(product)) {
                            val extraDrain = (product.currentStokJual * 0.6).roundToInt()
                            newStock -= extraDrain
                        }

                        if (newStock < 0) newStock = 0.0

                        productDao.updateProduct(
                            product.copy(
                                currentStokJual = newStock,
                                updatedAt = now
                            )
                        )
                    }
                }

                val totalTx = dayConfigs.sumOf { it.second }
                Log.d("TransactionSeeder", "Transaction seed complete! Total: $totalTx transactions over 7 days.")
            } catch (e: Exception) {
                Log.e("TransactionSeeder", "Error seeding transactions: ${e.message}", e)
            }
        }
    }

    // Reset product stocks to original CSV values
    private suspend fun resetProductStocks(context: android.content.Context, productDao: ProductDao) {
        try {
            val inputStream = context.assets.open("products.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val digitRegex = Regex("\\d+")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val now = sdf.format(Date())

            // Build a map of product name -> original stock from CSV
            val csvStockMap = mutableMapOf<String, Double>()
            var isFirstLine = true
            for (line in reader.lineSequence()) {
                if (isFirstLine) { isFirstLine = false; continue }
                val cols = line.split(",")
                if (cols.size >= 7) {
                    val name = "${cols[1].trim()} ${cols[2].trim()}"
                    val stock = digitRegex.find(cols[5])?.value?.toDoubleOrNull() ?: 0.0
                    csvStockMap[name] = stock
                }
            }
            reader.close()

            // Update all products with their original stock
            val allProducts = productDao.getAllProducts()
            for (product in allProducts) {
                val originalStock = csvStockMap[product.name]
                if (originalStock != null && originalStock != product.currentStokJual) {
                    productDao.updateProduct(product.copy(currentStokJual = originalStock, updatedAt = now))
                }
            }
            Log.d("TransactionSeeder", "Product stocks reset to original CSV values.")
        } catch (e: Exception) {
            Log.e("TransactionSeeder", "Error resetting stocks: ${e.message}", e)
        }
    }

    // Generate timestamps spread across store hours (06:00 - 20:00)
    // with realistic peaks (morning rush 07-09, lunch 11-13, afternoon 15-17)
    private fun generateTimestamps(dateStr: String, count: Int): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val cal = Calendar.getInstance()
        val dateParts = dateStr.split("-")

        val timestamps = mutableListOf<Long>()

        for (i in 0 until count) {
            cal.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())

            // Weighted hour selection to simulate real traffic
            val hour = pickHour(dateStr)
            val minute = random.nextInt(60)
            val second = random.nextInt(60)

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, second)

            timestamps.add(cal.timeInMillis)
        }

        timestamps.sort()
        return timestamps.map { sdf.format(Date(it)) }
    }

    // Weighted hour distribution (realistic market patterns)
    private fun pickHour(dateStr: String): Int {
        if (dateStr == "2026-05-27") {
            // Only open 08:00 - 11:59 on Eid al-Adha
            return 8 + random.nextInt(4)
        }
        // Store hours: 07:00 - 21:00
        val roll = random.nextInt(100)
        return when {
            roll < 10 -> 7                       // 10% early morning (07:xx)
            roll < 25 -> 8 + random.nextInt(2)   // 15% morning rush (08-09)
            roll < 40 -> 10 + random.nextInt(2)  // 15% late morning (10-11)
            roll < 55 -> 12 + random.nextInt(2)  // 15% lunch rush (12-13)
            roll < 65 -> 14 + random.nextInt(2)  // 10% early afternoon (14-15)
            roll < 80 -> 16 + random.nextInt(2)  // 15% afternoon rush (16-17)
            roll < 90 -> 18 + random.nextInt(1)  // 10% late afternoon (18)
            else      -> 19 + random.nextInt(2)  // 10% evening (19-20)
        }
    }

    // Weighted item count (most transactions have 1-3 items)
    private fun weightedItemCount(): Int {
        val roll = random.nextInt(100)
        return when {
            roll < 30 -> 1   // 30% — quick single item purchase
            roll < 65 -> 2   // 35% — two items
            roll < 85 -> 3   // 20% — three items
            roll < 95 -> 4   // 10% — four items
            else      -> 5   //  5% — five items
        }
    }

    // Pick products with bias toward hot-selling items
    private fun pickProducts(
        allProducts: List<ProductEntity>,
        hotProducts: List<ProductEntity>,
        promoProducts: List<ProductEntity>,
        count: Int,
        dateStr: String
    ): List<ProductEntity> {
        val selected = mutableSetOf<ProductEntity>()
        var attempts = 0
        
        val isPromoPeriod = dateStr >= "2026-05-27"

        while (selected.size < count && attempts < count * 5) {
            attempts++
            val product = if (isPromoPeriod && random.nextInt(100) < 60 && promoProducts.isNotEmpty()) {
                // 60% chance to pick promo products during promo period
                promoProducts[random.nextInt(promoProducts.size)]
            } else if (random.nextInt(100) < 70 && hotProducts.isNotEmpty()) {
                // 70% chance to pick from hot products
                hotProducts[random.nextInt(hotProducts.size)]
            } else {
                allProducts[random.nextInt(allProducts.size)]
            }
            // Only add if product has stock
            if (product.currentStokJual > 0) {
                selected.add(product)
            }
        }
        return selected.toList()
    }

    // Quantity per item (usually 1, more for cheap/staple items)
    private fun pickQuantity(product: ProductEntity): Int {
        val price = product.hargaJualDefault
        return when {
            price < 5000 -> {
                // Cheap items (instant noodles, sachets): 1-3
                1 + random.nextInt(3)
            }
            price < 15000 -> {
                // Medium price: 1-2
                1 + random.nextInt(2)
            }
            else -> {
                // Standard/Expensive items: 1
                1
            }
        }
    }

    // Payment method: ~75% CASH, ~18% QRIS, ~7% TRANSFER
    private fun pickPaymentMethod(): String {
        val roll = random.nextInt(100)
        return when {
            roll < 75 -> "CASH"
            roll < 93 -> "QRIS"
            else      -> "TRANSFER"
        }
    }

    // Calculate realistic paid amount
    // CASH: round up to nearest 1000 or 5000
    // QRIS/TRANSFER: exact amount
    private fun calculatePaidAmount(totalAmount: Double, method: String): Double {
        if (method != "CASH") return totalAmount

        // Round up to nearest convenient denomination
        val total = totalAmount.roundToLong()
        val roll = random.nextInt(100)

        return when {
            // 30% pay exact
            roll < 30 -> totalAmount
            // 40% round up to nearest 1000
            roll < 70 -> (ceil(total / 1000.0) * 1000).toDouble()
            // 20% round up to nearest 5000
            roll < 90 -> (ceil(total / 5000.0) * 5000).toDouble()
            // 10% round up to nearest 10000 or bigger note
            else -> {
                val rounded = (ceil(total / 10000.0) * 10000).toDouble()
                // Sometimes they give a 50k or 100k note
                when {
                    totalAmount < 20000 && random.nextBoolean() -> 20000.0
                    totalAmount < 50000 && random.nextBoolean() -> 50000.0
                    totalAmount < 100000 && random.nextBoolean() -> 100000.0
                    else -> rounded
                }
            }
        }
    }
}
