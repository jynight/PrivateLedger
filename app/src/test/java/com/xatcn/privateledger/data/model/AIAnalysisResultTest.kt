package com.xatcn.privateledger.data.model

import org.junit.Assert.*
import org.junit.Test

class AIAnalysisResultTest {

    @Test
    fun `创建 AI 分析结果 - 支出`() {
        val result = AIAnalysisResult(
            amount = 25.0f,
            category = "餐饮",
            type = TransactionType.EXPENSE,
            note = "午饭"
        )

        assertEquals(25.0f, result.amount, 0.01f)
        assertEquals("餐饮", result.category)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("午饭", result.note)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun `创建 AI 分析结果 - 收入`() {
        val result = AIAnalysisResult(
            amount = 8000.0f,
            category = "工资",
            type = TransactionType.INCOME,
            note = "月薪"
        )

        assertEquals(8000.0f, result.amount, 0.01f)
        assertEquals("工资", result.category)
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test
    fun `创建 AI 分析结果 - 自定义置信度`() {
        val result = AIAnalysisResult(
            amount = 100.0f,
            category = "购物",
            type = TransactionType.EXPENSE,
            note = "买书",
            confidence = 0.8f
        )

        assertEquals(0.8f, result.confidence, 0.01f)
    }
}
