package com.wickedcoder.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wickedcoder.app.core.util.DateUtils
import com.wickedcoder.app.data.local.dao.BankTotal
import com.wickedcoder.app.data.local.dao.CategoryTotal
import com.wickedcoder.app.data.local.entity.TransactionEntity
import com.wickedcoder.app.domain.model.PaymentMethod
import com.wickedcoder.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val totalSpentThisMonth: Double       = 0.0,
    val totalSpentLastMonth: Double       = 0.0,
    val monthlyBudget: Double             = 20_000.0,   // user-configurable later
    val spendByPaymentMethod: Map<PaymentMethod, Double> = emptyMap(),
    val topCategories: List<CategoryTotal> = emptyList(),
    val bankBreakdown: List<BankTotal>    = emptyList(),
    val isLoading: Boolean                = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Recent transactions are reactive — auto-refreshes when DB changes
    val recentTransactions: StateFlow<List<TransactionEntity>> =
        repository.getRecentTransactions(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadAggregates()
    }

    private fun loadAggregates() {
        val (thisStart, thisEnd) = DateUtils.currentMonthRange()
        val (lastStart, lastEnd) = DateUtils.lastMonthRange()

        viewModelScope.launch {
            val totalThis  = repository.getTotalSpent(thisStart, thisEnd)
            val totalLast  = repository.getTotalSpent(lastStart, lastEnd)
            val categories = repository.getSpendByCategory(thisStart, thisEnd).take(3)
            val banks      = repository.getSpendByBank(thisStart, thisEnd)
            val pmList     = repository.getSpendByPaymentMethod(thisStart, thisEnd)

            val pmMap = pmList.associate { pm ->
                (PaymentMethod.entries.firstOrNull { it.name == pm.paymentMethod }
                    ?: PaymentMethod.UNKNOWN) to pm.total
            }

            _uiState.update {
                it.copy(
                    totalSpentThisMonth  = totalThis,
                    totalSpentLastMonth  = totalLast,
                    spendByPaymentMethod = pmMap,
                    topCategories        = categories,
                    bankBreakdown        = banks,
                    isLoading            = false
                )
            }
        }
    }

    fun refresh() = loadAggregates()
}
