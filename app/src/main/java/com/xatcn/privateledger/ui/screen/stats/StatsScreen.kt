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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xatcn.privateledger.ui.theme.*
import com.xatcn.privateledger.ui.component.GlassmorphismCard
import com.xatcn.privateledger.util.DateUtils
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("month") }
    var totalIncome by remember { mutableDoubleStateOf(0.0) }
    var totalExpense by remember { mutableDoubleStateOf(0.0) }
    
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
                TrendChart()
            }
            
            // 类别占比
            item {
                CategoryBreakdown()
            }
            
            // 最大支出
            item {
                TopExpenses()
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
private fun TrendChart() {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "消费趋势",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 示例数据
        val data = listOf(150f, 230f, 180f, 320f, 280f, 190f, 260f)
        val labels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        
        SimpleLineChart(
            data = data,
            labels = labels,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
private fun SimpleLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f
    
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (data.size - 1)
            
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
private fun CategoryBreakdown() {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "类别占比",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 示例数据
        val categories = listOf(
            Triple("餐饮", 35f, ExpenseRed),
            Triple("交通", 20f, Color(0xFFFF9500)),
            Triple("购物", 25f, Color(0xFF5856D6)),
            Triple("娱乐", 15f, Color(0xFF34C759)),
            Triple("其他", 5f, Color(0xFF8E8E93))
        )
        
        categories.forEach { (name, percentage, color) ->
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
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TopExpenses() {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "最大支出",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 示例数据
        val expenses = listOf(
            Triple("房租", "¥2500.00", "05月01日"),
            Triple("购物", "¥580.00", "05月05日"),
            Triple("餐饮", "¥320.00", "05月08日")
        )
        
        expenses.forEach { (category, amount, date) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }
    }
}
