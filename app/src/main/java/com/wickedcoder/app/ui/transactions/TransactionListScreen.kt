package com.wickedcoder.app.ui.transactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wickedcoder.app.core.util.DateUtils
import com.wickedcoder.app.data.local.entity.TransactionEntity
import com.wickedcoder.app.domain.model.TxnType
import com.wickedcoder.app.ui.theme.*
import kotlinx.coroutines.launch

// ── Categories available for editing ─────────────────────────────────────────

private val allCategories = listOf(
    "Food & Dining" to "🍔",
    "Transport"     to "🚗",
    "Shopping"      to "🛍️",
    "Bills & Utilities" to "💡",
    "Entertainment" to "🎬",
    "Healthcare"    to "💊",
    "Investment"    to "📈",
    "Other"         to "📌"
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(vm: TransactionListViewModel = hiltViewModel()) {
    val listItems     by vm.listItems.collectAsStateWithLifecycle()
    val searchQuery   by vm.searchQuery.collectAsStateWithLifecycle()
    val selectedBank  by vm.selectedBank.collectAsStateWithLifecycle()
    val selectedType  by vm.selectedType.collectAsStateWithLifecycle()
    val dateFilter    by vm.dateFilter.collectAsStateWithLifecycle()
    val availBanks    by vm.availableBanks.collectAsStateWithLifecycle()
    val editingTxn    by vm.editingTxn.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    // Edit-category bottom sheet
    if (editingTxn != null) {
        EditCategorySheet(
            current    = editingTxn!!.category,
            onDismiss  = { vm.stopEditCategory() },
            onSelected = { cat -> vm.updateCategory(editingTxn!!.id, cat) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = vm::onSearchQuery,
                    placeholder   = { Text("Search merchants or banks…") },
                    leadingIcon   = { Icon(Icons.Default.Search, null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            // Filter chips row
            item {
                FilterRow(
                    selectedType   = selectedType,
                    selectedBank   = selectedBank,
                    dateFilter     = dateFilter,
                    availableBanks = availBanks,
                    onTypeSelected = vm::onTypeSelected,
                    onBankSelected = vm::onBankSelected,
                    onDateFilter   = vm::onDateFilter
                )
            }

            if (listItems.isEmpty()) {
                item {
                    Box(
                        Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(listItems, key = { item ->
                when (item) {
                    is TxnListItem.Header -> "header_${item.label}"
                    is TxnListItem.Row    -> "txn_${item.entity.id}"
                }
            }) { item ->
                when (item) {
                    is TxnListItem.Header -> DateHeader(item)
                    is TxnListItem.Row    -> SwipeableTransactionRow(
                        txn       = item.entity,
                        onDelete  = { txn ->
                            vm.deleteTransaction(txn) {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message     = "Transaction deleted",
                                        actionLabel = "Undo",
                                        duration    = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        vm.restoreTransaction(txn)
                                    }
                                }
                            }
                        },
                        onEditCategory = vm::startEditCategory
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Filter row ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selectedType: TxnType?,
    selectedBank: String?,
    dateFilter: DateFilter,
    availableBanks: List<String>,
    onTypeSelected: (TxnType?) -> Unit,
    onBankSelected: (String?) -> Unit,
    onDateFilter: (DateFilter) -> Unit
) {
    var showBankMenu by remember { mutableStateOf(false) }
    var showDateMenu by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Type: All | Debit | Credit
        listOf(null to "All", TxnType.DEBIT to "Debit", TxnType.CREDIT to "Credit").forEach { (type, label) ->
            FilterChip(
                selected = selectedType == type,
                onClick  = { onTypeSelected(if (selectedType == type) null else type) },
                label    = { Text(label) }
            )
        }

        // Bank dropdown
        Box {
            FilterChip(
                selected = selectedBank != null,
                onClick  = { showBankMenu = true },
                label    = { Text(selectedBank ?: "Bank") }
            )
            DropdownMenu(expanded = showBankMenu, onDismissRequest = { showBankMenu = false }) {
                DropdownMenuItem(text = { Text("All Banks") }, onClick = { onBankSelected(null); showBankMenu = false })
                availableBanks.forEach { bank ->
                    DropdownMenuItem(text = { Text(bank) }, onClick = { onBankSelected(bank); showBankMenu = false })
                }
            }
        }

        // Date filter dropdown
        Box {
            FilterChip(
                selected = true,
                onClick  = { showDateMenu = true },
                label    = { Text(dateFilter.label) }
            )
            DropdownMenu(expanded = showDateMenu, onDismissRequest = { showDateMenu = false }) {
                listOf(DateFilter.ThisMonth, DateFilter.LastMonth).forEach { f ->
                    DropdownMenuItem(
                        text    = { Text(f.label) },
                        onClick = { onDateFilter(f); showDateMenu = false }
                    )
                }
            }
        }
    }
}

// ── Date header ───────────────────────────────────────────────────────────────

@Composable
private fun DateHeader(header: TxnListItem.Header) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(header.label, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        if (header.debitTotal > 0) {
            Text(
                "−${DateUtils.formatAmount(header.debitTotal)}",
                style      = MaterialTheme.typography.labelMedium,
                color      = DebitRed,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Swipeable row ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionRow(
    txn: TransactionEntity,
    onDelete: (TransactionEntity) -> Unit,
    onEditCategory: (TransactionEntity) -> Unit
) {
    var dismissed by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    dismissed = true
                    onDelete(txn)
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEditCategory(txn)
                    false  // snap back — don't dismiss
                }
                else -> false
            }
        },
        positionalThreshold = { total -> total * 0.35f }
    )

    if (!dismissed) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val bgColor by animateColorAsState(
                    when (direction) {
                        SwipeToDismissBoxValue.EndToStart   -> DebitRed
                        SwipeToDismissBoxValue.StartToEnd   -> Indigo500
                        else -> Color.Transparent
                    }, label = "swipe_bg"
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = when (direction) {
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else                             -> Alignment.CenterStart
                    }
                ) {
                    Icon(
                        imageVector = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            else                             -> Icons.Default.Edit
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        ) {
            TransactionRow(txn)
        }
    }
}

// ── Transaction row card ──────────────────────────────────────────────────────

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
                Text(
                    txn.merchantNormalized,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 1
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(txn.bankName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("·", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(txn.category, style = MaterialTheme.typography.bodySmall, color = Indigo500)
                }
                Text(
                    DateUtils.formatTimestamp(txn.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
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

// ── Edit category bottom sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCategorySheet(
    current: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Text(
                "Edit Category",
                style    = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            allCategories.chunked(2).forEach { pair ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { (category, emoji) ->
                        val isSelected = category == current
                        OutlinedCard(
                            onClick   = { onSelected(category) },
                            modifier  = Modifier.weight(1f).padding(vertical = 4.dp),
                            colors    = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) Purple100 else Surface
                            ),
                            border    = if (isSelected)
                                CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Indigo500)
                                )
                            else CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(emoji, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    category,
                                    style      = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isSelected) Indigo700 else TextPrimary
                                )
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
