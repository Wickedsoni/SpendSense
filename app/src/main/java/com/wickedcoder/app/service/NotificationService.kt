package com.wickedcoder.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.wickedcoder.app.domain.usecase.ParseSmsAndSaveUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Captures bank notifications as a fallback for devices that restrict SMS access.
 * Requires the user to grant Notification Access in system settings.
 *
 * Full parsing logic will be added once SMS parsing is stable.
 */
@AndroidEntryPoint
class NotificationService : NotificationListenerService() {

    @Inject
    lateinit var parseAndSaveUseCase: ParseSmsAndSaveUseCase

    // SupervisorJob ensures one failed coroutine doesn't cancel the rest
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title") ?: ""
        val text  = extras.getCharSequence("android.text")?.toString() ?: ""

        // Only process notifications from known bank package names
        if (!isKnownBankPackage(sbn.packageName)) return

        serviceScope.launch {
            parseAndSaveUseCase(
                sender = sbn.packageName,  // use package name as the "sender" identifier
                body   = "$title $text"
            )
        }
    }

    private fun isKnownBankPackage(packageName: String): Boolean {
        val knownPackages = setOf(
            "com.snapwork.hdfc",          // HDFC Bank
            "com.sbi.SBIFreedomPlus",     // SBI YONO
            "com.icicibank.pocketbanking", // ICICI iMobile
            "com.axis.mobile",            // Axis Bank
            "com.kotak.mobile.banking",   // Kotak
            "com.phonepe.app",            // PhonePe
            "net.one97.paytm",            // Paytm
            "com.google.android.apps.nbu.paisa.user" // Google Pay
        )
        return packageName in knownPackages
    }
}
