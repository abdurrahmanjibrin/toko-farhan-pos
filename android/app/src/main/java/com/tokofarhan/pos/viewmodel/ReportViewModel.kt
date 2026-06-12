package com.tokofarhan.pos.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tokofarhan.pos.TokoFarhanApp
import com.tokofarhan.pos.data.local.dao.ProfitLossEntry
import com.tokofarhan.pos.data.local.entity.ProductEntity
import com.tokofarhan.pos.data.local.entity.TransactionEntity
import com.tokofarhan.pos.data.local.entity.TransactionItemEntity
import com.tokofarhan.pos.data.repository.ProductRepository
import com.tokofarhan.pos.data.repository.ReportRepository
import com.tokofarhan.pos.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TokoFarhanApp).database
    private val repo = ReportRepository(db.reportDao())
    private val txRepo = TransactionRepository(db.transactionDao(), db.productDao())
    private val productRepo = ProductRepository(db.productDao())
    
    var dailyEntries = mutableStateOf<List<ProfitLossEntry>>(emptyList())
    var lowStockCount = mutableStateOf(0)
    var lowStockProducts = mutableStateOf<List<ProductEntity>>(emptyList())
    var showLowStockDialog = mutableStateOf(false)
    var isLoading = mutableStateOf(false)
    var exportStatus = mutableStateOf<String?>(null)

    // Date filter
    var filterDate = mutableStateOf<String?>(null) // yyyy-MM-dd
    var showDatePicker = mutableStateOf(false)

    val filteredEntries: List<ProfitLossEntry>
        get() {
            val date = filterDate.value ?: return dailyEntries.value
            return dailyEntries.value.filter { it.date == date }
        }

    val todayRevenue: Double get() = dailyEntries.value.lastOrNull()?.totalRevenue ?: 0.0
    val todayProfit: Double get() = dailyEntries.value.lastOrNull()?.totalProfit ?: 0.0
    val totalProfit: Double get() = filteredEntries.sumOf { it.totalProfit }
    val totalRevenue: Double get() = filteredEntries.sumOf { it.totalRevenue }
    val totalTransactions: Int get() = filteredEntries.sumOf { it.totalTransactions }

    val chartData: List<Pair<String, Double>>
        get() = filteredEntries.takeLast(30).map {
            val label = try { 
                val parts = it.date.split("-")
                "${parts[2]}/${parts[1]}" 
            } catch (_: Exception) { it.date }
            Pair(label, it.totalRevenue)
        }

    init { loadReport() }

    fun loadReport() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                dailyEntries.value = repo.getDailyProfitLoss()
                lowStockCount.value = repo.getLowStockCount()
                lowStockProducts.value = productRepo.getLowStockProducts()
            } catch (_: Exception) {}
            finally { isLoading.value = false }
        }
    }

    fun filterByDate(date: String) {
        filterDate.value = date
    }

    fun clearFilter() {
        filterDate.value = null
    }

    // Get all transactions with items for Excel export
    suspend fun getAllTransactionsForExport(): List<Pair<TransactionEntity, List<TransactionItemEntity>>> {
        return txRepo.getAllTransactionsWithItems()
    }
}
