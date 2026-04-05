package com.wickedcoder.app.data.repository

import com.wickedcoder.app.data.local.dao.TransactionDao
import com.wickedcoder.app.data.local.entity.TransactionEntity
import com.wickedcoder.app.domain.model.ParsedTransaction
import com.wickedcoder.app.domain.model.TxnType
import com.wickedcoder.app.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    private val duplicateWindowMs = 30_000L

    override suspend fun saveTransaction(parsed: ParsedTransaction, sender: String): Boolean {
        if (isDuplicate(parsed)) return false
        dao.insert(parsed.toEntity(sender))
        return true
    }

    private suspend fun isDuplicate(parsed: ParsedTransaction): Boolean =
        dao.countSimilar(
            amount      = parsed.amount,
            merchantRaw = parsed.merchantRaw,
            from        = parsed.timestamp - duplicateWindowMs,
            to          = parsed.timestamp + duplicateWindowMs
        ) > 0

    private fun ParsedTransaction.toEntity(sender: String) = TransactionEntity(
        amount             = amount,
        merchantRaw        = merchantRaw,
        merchantNormalized = normalizeMerchant(merchantRaw),
        txnType            = txnType,
        bankName           = bankName,
        paymentMethod      = paymentMethod,
        timestamp          = timestamp,
        senderAddress      = sender
    )

    private fun normalizeMerchant(raw: String): String =
        raw.trim()
            .replace(Regex("[^A-Za-z0-9\\s&]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
            .take(30)

    override fun getAllTransactions()                         = dao.getAllTransactions()
    override fun getRecentTransactions(limit: Int)           = dao.getRecentTransactions(limit)
    override fun getByDateRange(from: Long, to: Long)        = dao.getByDateRange(from, to)
    override fun getByType(type: TxnType)                    = dao.getByType(type.name)
    override fun getByCategory(category: String)             = dao.getByCategory(category)
    override fun getRecurring()                              = dao.getRecurring()
    override suspend fun getTotalSpent(from: Long, to: Long) = dao.getTotalSpent(from, to)
    override suspend fun getTotalReceived(from: Long, to: Long) = dao.getTotalReceived(from, to)
    override suspend fun getSpendByCategory(from: Long, to: Long)      = dao.getSpendByCategory(from, to)
    override suspend fun getSpendByPaymentMethod(from: Long, to: Long) = dao.getSpendByPaymentMethod(from, to)
    override suspend fun getSpendByBank(from: Long, to: Long)          = dao.getSpendByBank(from, to)
    override suspend fun deleteTransaction(id: Long)                   = dao.deleteById(id)
    override suspend fun restoreTransaction(entity: TransactionEntity) { dao.insert(entity) }
    override suspend fun updateCategory(id: Long, category: String)    = dao.updateCategory(id, category)
    override suspend fun markRecurring(id: Long, isRecurring: Boolean) = dao.updateRecurring(id, isRecurring)
}
