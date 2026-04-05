package com.wickedcoder.app.domain.model

data class ParsedTransaction(
    val amount: Double,
    val merchantRaw: String,
    val txnType: TxnType,
    val bankName: String,
    val paymentMethod: PaymentMethod = PaymentMethod.UNKNOWN,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TxnType { DEBIT, CREDIT, UNKNOWN }
