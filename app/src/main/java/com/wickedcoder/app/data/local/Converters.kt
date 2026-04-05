package com.wickedcoder.app.data.local

import androidx.room.TypeConverter
import com.wickedcoder.app.domain.model.PaymentMethod
import com.wickedcoder.app.domain.model.TxnType

class Converters {
    @TypeConverter fun fromTxnType(value: TxnType): String = value.name
    @TypeConverter fun toTxnType(value: String): TxnType =
        TxnType.entries.firstOrNull { it.name == value } ?: TxnType.UNKNOWN

    @TypeConverter fun fromPaymentMethod(value: PaymentMethod): String = value.name
    @TypeConverter fun toPaymentMethod(value: String): PaymentMethod =
        PaymentMethod.entries.firstOrNull { it.name == value } ?: PaymentMethod.UNKNOWN
}
