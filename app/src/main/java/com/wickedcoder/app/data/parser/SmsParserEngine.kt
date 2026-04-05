package com.wickedcoder.app.data.parser

import com.wickedcoder.app.domain.model.ParsedTransaction
import com.wickedcoder.app.domain.model.PaymentMethod
import com.wickedcoder.app.domain.model.TxnType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParserEngine @Inject constructor() {

    fun parse(sender: String, body: String): ParsedTransaction? {
        if (!isTransactionSms(body)) return null
        return tryParseKnownBank(sender, body) ?: tryGenericParse(body)
    }

    private fun isTransactionSms(body: String): Boolean {
        val keywords = listOf("debited", "credited", "spent", "paid", "refund", "received", "rs.", "rs ", "inr", "upi")
        return keywords.any { body.lowercase().contains(it) }
    }

    // ── Known-bank rules ──────────────────────────────────────────────────────

    private fun tryParseKnownBank(sender: String, body: String): ParsedTransaction? {
        val s = sender.uppercase()
        return when {
            s.contains("HDFCBK") || s.contains("HDFC")   -> parseHdfc(body)
            s.contains("SBIIN")  || s.contains("SBIUPI")  -> parseSbi(body)
            s.contains("ICICIB") || s.contains("ICICI")   -> parseIcici(body)
            s.contains("AXISBK") || s.contains("AXIS")    -> parseAxis(body)
            else -> null
        }
    }

    private fun parseHdfc(body: String): ParsedTransaction? {
        val amount = extractAmount(body) ?: return null
        return ParsedTransaction(
            amount        = amount,
            merchantRaw   = extractMerchantAfterInfo(body) ?: extractMerchantAtTo(body) ?: "Unknown",
            txnType       = extractTxnType(body),
            bankName      = "HDFC Bank",
            paymentMethod = extractPaymentMethod(body)
        )
    }

    private fun parseSbi(body: String): ParsedTransaction? {
        val amount = extractAmount(body) ?: return null
        return ParsedTransaction(
            amount        = amount,
            merchantRaw   = extractMerchantAtTo(body) ?: extractUpiVpa(body) ?: "Unknown",
            txnType       = extractTxnType(body),
            bankName      = "State Bank of India",
            paymentMethod = extractPaymentMethod(body)
        )
    }

    private fun parseIcici(body: String): ParsedTransaction? {
        val amount = extractAmount(body) ?: return null
        return ParsedTransaction(
            amount        = amount,
            merchantRaw   = extractMerchantAtTo(body) ?: extractMerchantAfterInfo(body) ?: "Unknown",
            txnType       = extractTxnType(body),
            bankName      = "ICICI Bank",
            paymentMethod = extractPaymentMethod(body)
        )
    }

    private fun parseAxis(body: String): ParsedTransaction? {
        val amount = extractAmount(body) ?: return null
        return ParsedTransaction(
            amount        = amount,
            merchantRaw   = extractMerchantAtTo(body) ?: extractUpiVpa(body) ?: "Unknown",
            txnType       = extractTxnType(body),
            bankName      = "Axis Bank",
            paymentMethod = extractPaymentMethod(body)
        )
    }

    private fun tryGenericParse(body: String): ParsedTransaction? {
        val amount = extractAmount(body) ?: return null
        return ParsedTransaction(
            amount        = amount,
            merchantRaw   = extractMerchantAtTo(body) ?: extractMerchantAfterInfo(body) ?: extractUpiVpa(body) ?: "Unknown",
            txnType       = extractTxnType(body),
            bankName      = "Unknown",
            paymentMethod = extractPaymentMethod(body)
        )
    }

    // ── Extraction helpers ────────────────────────────────────────────────────

    private val amountRegex = Regex(
        """(?:Rs\.?|INR)\s*(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    private fun extractAmount(body: String): Double? =
        amountRegex.find(body)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

    private fun extractTxnType(body: String): TxnType {
        val lower = body.lowercase()
        return when {
            lower.contains(Regex("debited|spent|paid|purchase|withdrawn")) -> TxnType.DEBIT
            lower.contains(Regex("credited|received|refund|reversed"))     -> TxnType.CREDIT
            else -> TxnType.UNKNOWN
        }
    }

    private fun extractPaymentMethod(body: String): PaymentMethod {
        val lower = body.lowercase()
        return when {
            lower.contains("upi") || lower.contains("vpa") || lower.contains("@") -> PaymentMethod.UPI
            lower.contains("credit card") || lower.contains("debit card")
                || lower.contains(" card") || lower.contains("pos")               -> PaymentMethod.CARD
            lower.contains("neft") || lower.contains("imps") || lower.contains("rtgs") -> PaymentMethod.NET_BANKING
            else -> PaymentMethod.UNKNOWN
        }
    }

    private val atToRegex = Regex(
        """(?:at|to|from)\s+([A-Z][A-Za-z0-9\s\-&'.]{2,28})""",
        RegexOption.IGNORE_CASE
    )
    private fun extractMerchantAtTo(body: String): String? =
        atToRegex.find(body)?.groupValues?.get(1)?.trim()

    private val infoRegex = Regex(
        """Info[:\s]+(?:UPI|NEFT|IMPS)?[-\s]?([A-Za-z0-9\s\-&'.]{3,30})""",
        RegexOption.IGNORE_CASE
    )
    private fun extractMerchantAfterInfo(body: String): String? =
        infoRegex.find(body)?.groupValues?.get(1)?.trim()

    private val vpaRegex = Regex("""([A-Za-z0-9.\-_]{3,30}@[a-z]{2,10})""")
    private fun extractUpiVpa(body: String): String? =
        vpaRegex.find(body)?.groupValues?.get(1)?.trim()
}
