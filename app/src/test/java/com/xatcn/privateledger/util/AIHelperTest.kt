package com.xatcn.privateledger.util

import com.xatcn.privateledger.data.model.AIAnalysisResult
import com.xatcn.privateledger.data.model.TransactionType
import org.junit.Assert.*
import org.junit.Test

class AIHelperTest {

    @Test
    fun `解析 AI 响应 - 标准 JSON`() {
        val response = """{"amount": 25.0, "category": "餐饮", "type": "支出", "note": "午饭"}"""
        val result = AIHelper.parseAIResponse(response)

        assertNotNull(result)
        assertEquals(25.0f, result!!.amount, 0.01f)
        assertEquals("餐饮", result.category)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("午饭", result.note)
    }

    @Test
    fun `解析 AI 响应 - 收入类型`() {
        val response = """{"amount": 8000.0, "category": "工资", "type": "收入", "note": "月薪"}"""
        val result = AIHelper.parseAIResponse(response)

        assertNotNull(result)
        assertEquals(8000.0f, result!!.amount, 0.01f)
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test
    fun `解析 AI 响应 - 包含额外文字`() {
        val response = "这是解析结果：{\"amount\": 58.0, \"category\": \"购物\", \"type\": \"支出\", \"note\": \"买书\"}"
        val result = AIHelper.parseAIResponse(response)

        assertNotNull(result)
        assertEquals(58.0f, result!!.amount, 0.01f)
    }

    @Test
    fun `解析 AI 响应 - 无效 JSON`() {
        val response = "这不是有效的 JSON"
        val result = AIHelper.parseAIResponse(response)

        assertNull(result)
    }

    @Test
    fun `解析 AI 响应 - 空字符串`() {
        val response = ""
        val result = AIHelper.parseAIResponse(response)

        assertNull(result)
    }

    @Test
    fun `格式化确认消息 - 支出`() {
        val result = AIAnalysisResult(
            amount = 25.0f,
            category = "餐饮",
            type = TransactionType.EXPENSE,
            note = "午饭"
        )

        val message = AIHelper.formatConfirmation(result)

        assertTrue(message.contains("支出"))
        assertTrue(message.contains("25.00"))
        assertTrue(message.contains("餐饮"))
        assertTrue(message.contains("午饭"))
    }

    @Test
    fun `格式化确认消息 - 收入`() {
        val result = AIAnalysisResult(
            amount = 8000.0f,
            category = "工资",
            type = TransactionType.INCOME,
            note = "月薪"
        )

        val message = AIHelper.formatConfirmation(result)

        assertTrue(message.contains("收入"))
        assertTrue(message.contains("8000.00"))
    }
}
