package com.wickedcoder.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wickedcoder.app.domain.model.PaymentMethod
import com.wickedcoder.app.domain.model.TxnType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,

    @ColumnInfo(name = "merchant_raw")
    val merchantRaw: String,

    @ColumnInfo(name = "merchant_normalized")
    val merchantNormalized: String,

    @ColumnInfo(name = "txn_type")
    val txnType: TxnType,

    @ColumnInfo(name = "bank_name")
    val bankName: String,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: PaymentMethod = PaymentMethod.UNKNOWN,

    val category: String = "Uncategorized",

    val timestamp: Long,

    @ColumnInfo(name = "sender_address")
    val senderAddress: String,

    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false
)
