package com.wickedcoder.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wickedcoder.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE txn_type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE is_recurring = 1 ORDER BY merchant_normalized")
    fun getRecurring(): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE txn_type = 'DEBIT' AND timestamp BETWEEN :from AND :to")
    suspend fun getTotalSpent(from: Long, to: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE txn_type = 'CREDIT' AND timestamp BETWEEN :from AND :to")
    suspend fun getTotalReceived(from: Long, to: Long): Double

    @Query("""
        SELECT category, COALESCE(SUM(amount), 0.0) AS total
        FROM transactions
        WHERE txn_type = 'DEBIT' AND timestamp BETWEEN :from AND :to
        GROUP BY category ORDER BY total DESC
    """)
    suspend fun getSpendByCategory(from: Long, to: Long): List<CategoryTotal>

    @Query("""
        SELECT payment_method AS paymentMethod, COALESCE(SUM(amount), 0.0) AS total
        FROM transactions
        WHERE txn_type = 'DEBIT' AND timestamp BETWEEN :from AND :to
        GROUP BY payment_method
    """)
    suspend fun getSpendByPaymentMethod(from: Long, to: Long): List<PaymentMethodTotal>

    @Query("""
        SELECT bank_name AS bankName, COALESCE(SUM(amount), 0.0) AS total
        FROM transactions
        WHERE txn_type = 'DEBIT' AND timestamp BETWEEN :from AND :to
        GROUP BY bank_name ORDER BY total DESC LIMIT 5
    """)
    suspend fun getSpendByBank(from: Long, to: Long): List<BankTotal>

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE amount = :amount AND merchant_raw = :merchantRaw
        AND timestamp BETWEEN :from AND :to
    """)
    suspend fun countSimilar(amount: Double, merchantRaw: String, from: Long, to: Long): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE transactions SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String)

    @Query("UPDATE transactions SET is_recurring = :isRecurring WHERE id = :id")
    suspend fun updateRecurring(id: Long, isRecurring: Boolean)
}

data class CategoryTotal(val category: String, val total: Double)
data class PaymentMethodTotal(val paymentMethod: String, val total: Double)
data class BankTotal(val bankName: String, val total: Double)
