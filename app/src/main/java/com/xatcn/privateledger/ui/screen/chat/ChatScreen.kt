package com.xatcn.privateledger.ui.screen.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xatcn.privateledger.PrivateLedgerApp
import com.xatcn.privateledger.data.model.AIAnalysisResult
import com.xatcn.privateledger.data.model.Transaction
import com.xatcn.privateledger.data.model.TransactionSource
import com.xatcn.privateledger.data.model.TransactionType
import com.xatcn.privateledger.service.AIService
import com.xatcn.privateledger.ui.theme.*
import com.xatcn.privateledger.ui.component.GlassmorphismCard
import com.xatcn.privateledger.ui.component.LoadingIndicator
import com.xatcn.privateledger.util.SpeechHelper
import com.xatcn.privateledger.util.SpeechState
import kotlinx.coroutines.launch

sealed class ChatMessage {
    data class UserMessage(val text: String) : ChatMessage()
    data class AIMessage(val text: String) : ChatMessage()
    data class ConfirmationCard(val result: AIAnalysisResult) : ChatMessage()
    data class SystemMessage(val text: String) : ChatMessage()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PrivateLedgerApp
    val transactionRepo = app.transactionRepository
    val aiService = remember { AIService.getInstance(context) }
    val speechHelper = remember { SpeechHelper(context) }
    val scope = rememberCoroutineScope()

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // 监听语音识别状态
    val speechState by speechHelper.state.collectAsState()

    // 语音识别结果处理
    LaunchedEffect(speechState) {
        when (val state = speechState) {
            is SpeechState.Success -> {
                inputText = state.text
                speechHelper.reset()
            }
            is SpeechState.Error -> {
                messages = messages + ChatMessage.SystemMessage("❌ 语音识别失败：${state.message}")
                speechHelper.reset()
            }
            else -> {}
        }
    }

    // 语音识别
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!results.isNullOrEmpty()) {
            inputText = results[0]
        }
    }

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出您的消费或收入信息")
            }
            speechLauncher.launch(intent)
        }
    }

    // 自动滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI 记账助手",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (aiService.hasModel()) "AI 模式" else "规则模式",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 欢迎消息
                if (messages.isEmpty()) {
                    item {
                        WelcomeMessage(hasModel = aiService.hasModel())
                    }
                }

                items(messages) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        when (message) {
                            is ChatMessage.UserMessage -> UserMessageBubble(message.text)
                            is ChatMessage.AIMessage -> AIMessageBubble(message.text)
                            is ChatMessage.ConfirmationCard -> ConfirmationCardView(
                                result = message.result,
                                onConfirm = {
                                    // 真正插入数据库！
                                    scope.launch {
                                        try {
                                            val transaction = Transaction(
                                                amount = message.result.amount.toDouble(),
                                                category = message.result.category,
                                                type = message.result.type,
                                                note = message.result.note,
                                                source = TransactionSource.AI_CHAT
                                            )
                                            val id = transactionRepo.insertTransaction(transaction)
                                            messages = messages + ChatMessage.SystemMessage("✅ 已记录 (ID: $id)")
                                            messages = messages + ChatMessage.AIMessage(
                                                "已为您记录这笔${if (message.result.type == TransactionType.INCOME) "收入" else "支出"}！\n" +
                                                "金额：¥${String.format("%.2f", message.result.amount)}\n" +
                                                "类别：${message.result.category}"
                                            )
                                        } catch (e: Exception) {
                                            messages = messages + ChatMessage.SystemMessage("❌ 保存失败：${e.message}")
                                        }
                                    }
                                },
                                onCancel = {
                                    messages = messages + ChatMessage.SystemMessage("❌ 已取消")
                                }
                            )
                            is ChatMessage.SystemMessage -> SystemMessageView(message.text)
                        }
                    }
                }

                // 加载指示器
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }
            }

            // 输入栏
            ChatInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val userText = inputText
                        inputText = ""
                        messages = messages + ChatMessage.UserMessage(userText)
                        isLoading = true

                        // 调用 AI 解析
                        scope.launch {
                            messages = messages + ChatMessage.AIMessage("正在分析您的输入...")

                            val result = aiService.analyzeTransaction(userText)
                            result.fold(
                                onSuccess = { analysisResult ->
                                    // 移除"正在分析"消息
                                    messages = messages.dropLast(1)
                                    // 添加确认卡片
                                    messages = messages + ChatMessage.ConfirmationCard(analysisResult)
                                },
                                onFailure = { e ->
                                    // 移除"正在分析"消息
                                    messages = messages.dropLast(1)
                                    messages = messages + ChatMessage.AIMessage("抱歉，无法解析您的输入：${e.message}")
                                    messages = messages + ChatMessage.AIMessage("请尝试更清晰的描述，例如：\n• 今天午饭花了25块\n• 收到工资8000元")
                                }
                            )
                            isLoading = false
                        }
                    }
                },
                onVoiceClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                        speechHelper.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onImageClick = {
                    // TODO: 启动图片选择
                }
            )
        }
    }
}

@Composable
private fun WelcomeMessage(hasModel: Boolean) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🤖",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "你好！我是你的 AI 记账助手",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasModel) "当前已加载 AI 模型，可以智能分析您的记账信息"
                   else "当前使用规则解析模式，您也可以导入 AI 模型获得更好的体验",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "你可以直接告诉我你的消费或收入情况，比如：",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• 今天午饭花了25块\n• 收到工资8000元\n• 买了本书58元",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(KleinBlue, KleinBlueLight)
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun AIMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(KleinBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🤖", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ConfirmationCardView(
    result: AIAnalysisResult,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "📝 记账确认",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "类型",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (result.type == TransactionType.INCOME) "收入" else "支出",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Column {
                Text(
                    text = "金额",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥${String.format("%.2f", result.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (result.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            }

            Column {
                Text(
                    text = "类别",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (result.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "备注：${result.note}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = KleinBlue)
            ) {
                Text("确认记账")
            }
        }
    }
}

@Composable
private fun SystemMessageView(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图片按钮
            IconButton(onClick = onImageClick) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "图片",
                    tint = KleinBlue
                )
            }

            // 输入框
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消费或收入...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 语音/发送按钮
            if (value.isBlank()) {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(KleinBlue)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "语音",
                        tint = Color.White
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(KleinBlue)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
