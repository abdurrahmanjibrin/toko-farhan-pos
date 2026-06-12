package com.tokofarhan.pos.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tokofarhan.pos.TokoFarhanApp
import com.tokofarhan.pos.data.local.entity.TransactionEntity
import com.tokofarhan.pos.data.local.entity.TransactionItemEntity
import com.tokofarhan.pos.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TokoFarhanApp).database
    private val repo = TransactionRepository(db.transactionDao(), db.productDao())
    
    var transactions = mutableStateOf<List<TransactionEntity>>(emptyList())
    var isLoading = mutableStateOf(false)
    var selectedDetail = mutableStateOf<Pair<TransactionEntity, List<TransactionItemEntity>>?>(null)
    var showDetail = mutableStateOf(false)

    // Date filter
    var filterDate = mutableStateOf<String?>(null) // yyyy-MM-dd format
    var showDatePicker = mutableStateOf(false)

    init { loadTransactions() }

    fun loadTransactions() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val date = filterDate.value
                transactions.value = if (date != null) {
                    repo.getTransactionsByDate(date)
                } else {
                    repo.getTransactions()
                }
            } catch (_: Exception) {}
            finally { isLoading.value = false }
        }
    }

    fun filterByDate(date: String) {
        filterDate.value = date
        loadTransactions()
    }

    fun clearFilter() {
        filterDate.value = null
        loadTransactions()
    }

    fun loadDetail(transactionId: String) {
        viewModelScope.launch {
            try {
                selectedDetail.value = repo.getTransactionDetail(transactionId)
                showDetail.value = true
            } catch (_: Exception) {}
        }
    }

    fun dismissDetail() { showDetail.value = false; selectedDetail.value = null }
}
