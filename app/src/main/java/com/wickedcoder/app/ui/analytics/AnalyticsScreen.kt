package com.wickedcoder.app.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wickedcoder.app.ui.theme.TextSecondary

@Composable
fun AnalyticsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Analytics", style = MaterialTheme.typography.titleLarge)
            Text("Bar chart · Donut · Line chart — Coming in Step 12",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
