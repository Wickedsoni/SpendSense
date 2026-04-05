package com.wickedcoder.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wickedcoder.app.core.util.DateUtils
import com.wickedcoder.app.data.local.dao.BankTotal
import com.wickedcoder.app.data.local.dao.CategoryTotal
import com.wickedcoder.app.data.local.entity.TransactionEntity
import com.wickedcoder.app.domain.model.PaymentMethod
import com.wickedcoder.app.domain.model.TxnType
import com.wickedcoder.app.ui.theme.*

@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val state  by vm.uiState.collectAsStateWithLifecycle()
    val recent by vm.recentTransactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MonthlySpendCard(state) }
        item { PaymentMethodSection(state.spendByPaymentMethod) }
        if (state.topCategories.isNotEmpty()) {
            item { TopCategoriesSection(state.topCategories) }
        }
        item { SectionHeader("Recent Transactions") }
        if (recent.isEmpty()) {
            item {
                Text(
                    "No transactions yet. SMS will appear here automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(recent) { txn -> TransactionRow(txn) }
        }
        if (state.bankBreakdown.isNotEmpty()) {
            item { BankBreakdownSection(state.bankBreakdown, state.totalSpentThisMonth) }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ── Monthly Spend Card ────────────────────────────────────────────────────────

@Composable
private fun MonthlySpendCard(state: DashboardUiState) {
    val changePercent = if (state.totalSpentLastMonth > 0)
        ((state.totalSpentThisMonth - state.totalSpentLastMonth) / state.totalSpentLastMonth * 100)
    else 0.0
    val budgetProgress = (state.totalSpentThisMonth / state.monthlyBudget).coerceIn(0.0, 1.0)
    val budgetColor    = when {
        budgetProgress >= 0.9 -> DebitRed
        budgetProgress >= 0.7 -> WarningAmber
        else                  -> CreditGreen
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Indigo500),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("This Month's Spend", style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.8f))

            Text(DateUtils.formatAmount(state.totalSpentThisMonth),
                style     = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold, color = Color.White)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (changePercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint   = if (changePercent >= 0) Color(0xFFFFCDD2) else Color(0xFFC8E6C9),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "%+.1f%% vs last month".format(changePercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budget: ${DateUtils.formatAmount(state.monthlyBudget)}",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                Text("%.0f%%".format(budgetProgress * 100),
                    style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
            LinearProgressIndicator(
                progress          = { budgetProgress.toFloat() },
                modifier          = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color             = budgetColor,
                trackColor        = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

// ── Payment Method Section ────────────────────────────────────────────────────

@Composable
private fun PaymentMethodSection(spend: Map<PaymentMethod, Double>) {
    SectionHeader("Payment Breakdown")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PaymentMethodCard("UPI",         Icons.Default.PhoneAndroid, UpiBlue,    spend[PaymentMethod.UPI] ?: 0.0)
        PaymentMethodCard("Card",        Icons.Default.CreditCard,   CardOrange, spend[PaymentMethod.CARD] ?: 0.0)
        PaymentMethodCard("Net Banking", Icons.Default.Money,        NetBankingPurple, spend[PaymentMethod.NET_BANKING] ?: 0.0)
        PaymentMethodCard("Cash / Other",Icons.Default.Money,        CashTeal,   spend[PaymentMethod.CASH] ?: 0.0 + (spend[PaymentMethod.UNKNOWN] ?: 0.0))
    }
}

@Composable
private fun PaymentMethodCard(label: String, icon: ImageVector, color: Color, amount: Double) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment       = Alignment.CenterVertically,
            horizontalArrangement   = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(12.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text(
                DateUtils.formatAmount(amount),
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = if (amount > 0) TextPrimary else TextSecondary
            )
        }
    }
}

// ── Top Categories ────────────────────────────────────────────────────────────

@Composable
private fun TopCategoriesSection(categories: List<CategoryTotal>) {
    SectionHeader("Top Categories")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { ct ->
            AssistChip(
                onClick   = {},
                label     = { Text("${ct.category}  ${DateUtils.formatAmount(ct.total)}") },
                colors    = AssistChipDefaults.assistChipColors(
                    containerColor = Purple100,
                    labelColor     = Indigo700
                )
            )
        }
    }
}

// ── Recent Transactions ───────────────────────────────────────────────────────

@Composable
private fun TransactionRow(txn: TransactionEntity) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(txn.merchantNormalized,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                    maxLines = 1)
                Text("${txn.bankName} · ${DateUtils.formatTimestamp(txn.timestamp)}",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(
                (if (txn.txnType == TxnType.DEBIT) "−" else "+") + DateUtils.formatAmount(txn.amount),
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = if (txn.txnType == TxnType.DEBIT) DebitRed else CreditGreen
            )
        }
    }
}

// ── Bank Breakdown ────────────────────────────────────────────────────────────

@Composable
private fun BankBreakdownSection(banks: List<BankTotal>, totalSpent: Double) {
    SectionHeader("Bank Breakdown")
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            banks.forEach { bank ->
                val fraction = if (totalSpent > 0) (bank.total / totalSpent).toFloat() else 0f
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(bank.bankName, style = MaterialTheme.typography.bodySmall)
                        Text(DateUtils.formatAmount(bank.total),
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    LinearProgressIndicator(
                        progress  = { fraction },
                        modifier  = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color     = Indigo500,
                        trackColor = Divider
                    )
                }
            }
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 2.dp))
}
