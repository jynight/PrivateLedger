package com.xatcn.privateledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val note: String = "",
    val date: String = LocalDateTime.now().toString(),
    val source: TransactionSource = TransactionSource.MANUAL,
    val isReversed: Boolean = false,
    val reversedBy: Long? = null,
    val originalTransactionId: Long? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionSource {
    MANUAL,      // 手动输入
    AI_CHAT,     // AI 对话
    OCR,         // OCR 识别
    ACCESSIBILITY // 无障碍抓取
}
