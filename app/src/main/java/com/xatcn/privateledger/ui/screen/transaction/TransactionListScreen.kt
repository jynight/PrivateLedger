package com.xatcn.privateledger.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xatcn.privateledger.data.model.Transaction
import com.xatcn.privateledger.data.model.TransactionType
import com.xatcn.privateledger.ui.theme.*
import com.xatcn.privateledger.ui.component.GlassmorphismCard
import com.xatcn.privateledger.util.DateUtils
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactions: List<Transaction>,
    onNavigateBack: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onReverseTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    var showReverseDialog by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "账单记录",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KleinBlue,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无账单记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEdit = { onEditTransaction(transaction) },
                        onReverse = { showReverseDialog = transaction },
                        onDelete = { showDeleteDialog = transaction }
                    )
                }
            }
        }
    }
    
    // 冲销确认对话框
    showReverseDialog?.let { transaction ->
        AlertDialog(
            onDismissRequest = { showReverseDialog = null },
            title = { Text("确认冲销") },
            text = {
                Column {
                    Text("确定要冲销这笔${if (transaction.type == TransactionType.INCOME) "收入" else "支出"}吗？")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "金额：¥${String.format("%.2f", transaction.amount)}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "类别：${transaction.category}")
                    if (transaction.note.isNotBlank()) {
                        Text(text = "备注：${transaction.note}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ 冲销将生成一笔反向金额的记录，原记录将被标记为已冲销",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReverseTransaction(transaction)
                        showReverseDialog = null
                    }
                ) {
                    Text("确认冲销", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReverseDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 删除确认对话框
    showDeleteDialog?.let { transaction ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = {
                Column {
                    Text("确定要删除这笔记录吗？")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "金额：¥${String.format("%.2f", transaction.amount)}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "类别：${transaction.category}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ 此操作不可撤销",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(transaction)
                        showDeleteDialog = null
                    }
                ) {
                    Text("确认删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onReverse: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isReversed) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (transaction.type == TransactionType.INCOME) 
                        Icons.Default.TrendingUp 
                    else 
                        Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 交易信息
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (transaction.isReversed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "已冲销",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = DateUtils.formatForDisplay(LocalDateTime.parse(transaction.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 金额
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}¥${String.format("%.2f", kotlin.math.abs(transaction.amount))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
                
                // 来源标签
                Text(
                    text = when (transaction.source) {
                        com.xatcn.privateledger.data.model.TransactionSource.MANUAL -> "手动"
                        com.xatcn.privateledger.data.model.TransactionSource.AI_CHAT -> "AI"
                        com.xatcn.privateledger.data.model.TransactionSource.OCR -> "OCR"
                        com.xatcn.privateledger.data.model.TransactionSource.ACCESSIBILITY -> "自动"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 菜单
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    
                    if (!transaction.isReversed) {
                        DropdownMenuItem(
                            text = { Text("冲销") },
                            onClick = {
                                showMenu = false
                                onReverse()
                            },
                            leadingIcon = { Icon(Icons.Default.Undo, contentDescription = null) }
                        )
                    }
                    
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        }
                    )
                }
            }
        }
    }
}
