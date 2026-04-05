package com.wickedcoder.app.domain.usecase

import com.wickedcoder.app.data.parser.SmsParserEngine
import com.wickedcoder.app.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Single-responsibility use case: parse an incoming SMS and persist it.
 *
 * Returns:
 *   true  — transaction was parsed and saved successfully
 *   false — SMS was not a transaction, or it was a duplicate
 */
class ParseSmsAndSaveUseCase @Inject constructor(
    private val parser: SmsParserEngine,
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(sender: String, body: String): Boolean {
        val parsed = parser.parse(sender, body) ?: return false
        return repository.saveTransaction(parsed, sender)
    }
}
