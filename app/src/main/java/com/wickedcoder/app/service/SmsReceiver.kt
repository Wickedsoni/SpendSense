package com.wickedcoder.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.wickedcoder.app.domain.usecase.ParseSmsAndSaveUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var parseAndSaveUseCase: ParseSmsAndSaveUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        // Multi-part SMS arrives as separate SmsMessage objects — join them into one body
        val fullBody = messages.joinToString("") { it.messageBody }
        val sender = messages.firstOrNull()?.originatingAddress ?: return

        // goAsync() tells the system "don't kill me yet, I have async work to finish"
        // Without this, the BroadcastReceiver is considered done the moment onReceive() returns
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                parseAndSaveUseCase(sender, fullBody)
            } finally {
                pendingResult.finish()  // must always call this or the system ANRs after ~10s
            }
        }
    }
}
