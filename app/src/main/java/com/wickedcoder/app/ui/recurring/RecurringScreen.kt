package com.wickedcoder.app.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wickedcoder.app.ui.theme.TextSecondary

@Composable
fun RecurringScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Recurring & Subscriptions", style = MaterialTheme.typography.titleLarge)
            Text("Auto-detected by PatternAnalysisWorker — Coming in Step 13",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
