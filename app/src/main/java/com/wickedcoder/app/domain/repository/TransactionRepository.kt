package com.wickedcoder.app.domain.repository

import com.wickedcoder.app.data.local.dao.BankTotal
import com.wickedcoder.app.data.local.dao.CategoryTotal
import com.wickedcoder.app.data.local.dao.PaymentMethodTotal
import com.wickedcoder.app.data.local.entity.TransactionEntity
import com.wickedcoder.app.domain.model.ParsedTransaction
import com.wickedcoder.app.domain.model.TxnType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun saveTransaction(parsed: ParsedTransaction, sender: String): Boolean

    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>
    fun getByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>>
    fun getByType(type: TxnType): Flow<List<TransactionEntity>>
    fun getByCategory(category: String): Flow<List<TransactionEntity>>
    fun getRecurring(): Flow<List<TransactionEntity>>

    suspend fun getTotalSpent(from: Long, to: Long): Double
    suspend fun getTotalReceived(from: Long, to: Long): Double
    suspend fun getSpendByCategory(from: Long, to: Long): List<CategoryTotal>
    suspend fun getSpendByPaymentMethod(from: Long, to: Long): List<PaymentMethodTotal>
    suspend fun getSpendByBank(from: Long, to: Long): List<BankTotal>

    suspend fun deleteTransaction(id: Long)
    suspend fun restoreTransaction(entity: TransactionEntity)   // undo-delete
    suspend fun updateCategory(id: Long, category: String)
    suspend fun markRecurring(id: Long, isRecurring: Boolean)
}
