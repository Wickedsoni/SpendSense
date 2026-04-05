package com.wickedcoder.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wickedcoder.app.core.util.DateUtils
import com.wickedcoder.app.data.local.entity.TransactionEntity
import com.wickedcoder.app.domain.model.TxnType
import com.wickedcoder.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Date filter ───────────────────────────────────────────────────────────────

sealed class DateFilter(val label: String) {
    data object ThisMonth : DateFilter("This Month")
    data object LastMonth : DateFilter("Last Month")
    data class Custom(val from: Long, val to: Long) : DateFilter("Custom")

    fun toRange(): Pair<Long, Long> = when (this) {
        is ThisMonth -> DateUtils.currentMonthRange()
        is LastMonth -> DateUtils.lastMonthRange()
        is Custom    -> from to to
    }
}

// ── List items (header + row) ─────────────────────────────────────────────────

sealed class TxnListItem {
    data class Header(val label: String, val debitTotal: Double) : TxnListItem()
    data class Row(val entity: TransactionEntity) : TxnListItem()
}

private fun List<TransactionEntity>.toGrouped(): List<TxnListItem> {
    if (isEmpty()) return emptyList()
    val result  = mutableListOf<TxnListItem>()
    val grouped = groupBy { DateUtils.formatDay(it.timestamp) }
    grouped.forEach { (label, txns) ->
        val dayDebit = txns.filter { it.txnType == TxnType.DEBIT }.sumOf { it.amount }
        result += TxnListItem.Header(label, dayDebit)
        result += txns.map { TxnListItem.Row(it) }
    }
    return result
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _searchQuery   = MutableStateFlow("")
    private val _selectedBank  = MutableStateFlow<String?>(null)
    private val _selectedType  = MutableStateFlow<TxnType?>(null)
    private val _dateFilter    = MutableStateFlow<DateFilter>(DateFilter.ThisMonth)
    private val _editingTxn    = MutableStateFlow<TransactionEntity?>(null)

    val searchQuery   = _searchQuery.asStateFlow()
    val selectedBank  = _selectedBank.asStateFlow()
    val selectedType  = _selectedType.asStateFlow()
    val dateFilter    = _dateFilter.asStateFlow()
    val editingTxn    = _editingTxn.asStateFlow()

    private val allTxns = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Distinct bank names for the filter dropdown. */
    val availableBanks = allTxns
        .map { list -> list.map { it.bankName }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Filtered + grouped items driving the LazyColumn. */
    val listItems = combine(
        allTxns,
        _searchQuery.debounce(250),
        _selectedBank,
        _selectedType,
        _dateFilter
    ) { txns, query, bank, type, dateFilter ->
        val (from, to) = dateFilter.toRange()
        txns.filter { t ->
            (query.isBlank() || t.merchantNormalized.contains(query, ignoreCase = true) ||
                    t.bankName.contains(query, ignoreCase = true)) &&
            (bank == null || t.bankName == bank) &&
            (type == null || t.txnType == type) &&
            (t.timestamp in from..to)
        }.toGrouped()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Actions ───────────────────────────────────────────────────────────────

    fun onSearchQuery(q: String)             { _searchQuery.value  = q    }
    fun onBankSelected(bank: String?)        { _selectedBank.value = bank }
    fun onTypeSelected(type: TxnType?)       { _selectedType.value = type }
    fun onDateFilter(f: DateFilter)          { _dateFilter.value   = f    }
    fun startEditCategory(t: TransactionEntity) { _editingTxn.value = t  }
    fun stopEditCategory()                   { _editingTxn.value   = null }

    fun deleteTransaction(txn: TransactionEntity, onUndo: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransaction(txn.id)
            onUndo()   // caller shows snackbar; if pressed → restoreTransaction
        }
    }

    fun restoreTransaction(txn: TransactionEntity) {
        viewModelScope.launch { repository.restoreTransaction(txn) }
    }

    fun updateCategory(id: Long, category: String) {
        viewModelScope.launch {
            repository.updateCategory(id, category)
            stopEditCategory()
        }
    }
}
