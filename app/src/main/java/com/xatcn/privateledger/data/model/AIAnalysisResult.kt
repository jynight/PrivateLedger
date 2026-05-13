package com.xatcn.privateledger.data.model

data class AIAnalysisResult(
    val amount: Float,
    val category: String,
    val type: TransactionType,
    val note: String,
    val confidence: Float = 1.0f
)
