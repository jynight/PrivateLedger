package com.xatcn.privateledger.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xatcn.privateledger.PrivateLedgerApp
import com.xatcn.privateledger.data.model.TransactionType
import com.xatcn.privateledger.ui.theme.*
import com.xatcn.privateledger.ui.component.GlassmorphismCard
import com.xatcn.privateledger.util.DateUtils
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PrivateLedgerApp
    val transactionRepo = app.transactionRepository

    val allTransactions by transactionRepo.getAllTransactions().collectAsState(initial = emptyList())
    var selectedPeriod by remember { mutableStateOf("month") }

    // 根据选择的时间段过滤
    val now = LocalDate.now()
    val filteredTransactions = remember(allTransactions, selectedPeriod) {
        val activeTransactions = allTransactions.filter { !it.isReversed }
        when (selectedPeriod) {
            "week" -> {
                val weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                activeTransactions.filter {
                    val dateStr = it.date.substringBefore("T").take(10)
                    val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { return@filter false }
                    !date.isBefore(weekStart) && !date.isAfter(now)
                }
            }
            "month" -> {
                val monthStart = now.withDayOfMonth(1)
                activeTransactions.filter {
                    val dateStr = it.date.substringBefore("T").take(10)
                    val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { return@filter false }
                    !date.isBefore(monthStart) && !date.isAfter(now)
                }
            }
            "year" -> {
                val yearStart = now.withDayOfYear(1)
                activeTransactions.filter {
                    val dateStr = it.date.substringBefore("T").take(10)
                    val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { return@filter false }
                    !date.isBefore(yearStart) && !date.isAfter(now)
                }
            }
            else -> activeTransactions
        }
    }

    val totalIncome = filteredTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    val totalExpense = filteredTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    // 类别统计
    val categoryStats = filteredTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .map { (category, transactions) ->
            Triple(category, transactions.sumOf { it.amount }, transactions.size)
        }
        .sortedByDescending { it.second }

    // 最大支出 Top 5
    val topExpenses = filteredTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sortedByDescending { it.amount }
        .take(5)

    // 最近7天消费趋势
    val last7Days = (0..6).map { now.minusDays(it.toLong()) }.reversed()
    val dailyExpenses = last7Days.map { day ->
        filteredTransactions
            .filter {
                it.type == TransactionType.EXPENSE && it.date.startsWith(day.toString())
            }
            .sumOf { it.amount }
            .toFloat()
    }
    val dayLabels = last7Days.map { "${it.monthValue}/${it.dayOfMonth}" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "统计报表",
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
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 时间选择器
            item {
                PeriodSelector(
                    selected = selectedPeriod,
                    onSelect = { selectedPeriod = it }
                )
            }

            // 概览卡片
            item {
                StatsOverviewCard(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )
            }

            // 趋势图
            item {
                TrendChart(
                    data = dailyExpenses,
                    labels = dayLabels
                )
            }

            // 类别占比
            item {
                CategoryBreakdown(
                    categories = categoryStats,
                    totalExpense = totalExpense
                )
            }

            // 最大支出
            item {
                TopExpenses(expenses = topExpenses)
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "week" to "本周",
            "month" to "本月",
            "year" to "本年"
        ).forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KleinBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun StatsOverviewCard(
    totalIncome: Double,
    totalExpense: Double
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "收支概览",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "总收入",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥${String.format("%.2f", totalIncome)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = IncomeGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "结余",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥${String.format("%.2f", totalIncome - totalExpense)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (totalIncome - totalExpense >= 0) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "总支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥${String.format("%.2f", totalExpense)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TrendChart(
    data: List<Float>,
    labels: List<String>
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "消费趋势",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (data.all { it == 0f }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            SimpleLineChart(
                data = data,
                labels = labels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            // 绘制网格线
            for (i in 0..4) {
                val y = height * i / 4
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // 绘制折线
            val path = Path()
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - (value / maxValue * height)
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = KleinBlue,
                style = Stroke(width = 3f)
            )

            // 绘制数据点
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - (value / maxValue * height)
                drawCircle(
                    color = KleinBlue,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }

        // 标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdown(
    categories: List<Triple<String, Double, Int>>,
    totalExpense: Double
) {
    val colors = listOf(
        ExpenseRed,
        Color(0xFFFF9500),
        Color(0xFF5856D6),
        Color(0xFF34C759),
        Color(0xFF8E8E93),
        Color(0xFF007AFF),
        Color(0xFFFF2D55)
    )

    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "类别占比",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            categories.forEachIndexed { index, (name, amount, count) ->
                val percentage = if (totalExpense > 0) (amount / totalExpense * 100) else 0.0
                val color = colors[index % colors.size]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "¥${String.format("%.0f", amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TopExpenses(
    expenses: List<com.xatcn.privateledger.data.model.Transaction>
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "最大支出",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            expenses.forEach { transaction ->
                val dateStr = try {
                    val dt = LocalDateTime.parse(transaction.date)
                    dt.format(DateTimeFormatter.ofPattern("MM月dd日"))
                } catch (e: Exception) {
                    transaction.date.take(10)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "¥${String.format("%.2f", transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}
