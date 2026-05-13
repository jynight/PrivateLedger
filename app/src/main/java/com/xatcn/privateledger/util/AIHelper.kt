package com.xatcn.privateledger.util

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.xatcn.privateledger.data.model.AIAnalysisResult
import com.xatcn.privateledger.data.model.TransactionType

object AIHelper {
    
    private val gson = Gson()
    
    // System Prompt for LLM
    const val SYSTEM_PROMPT = """你是一个记账助手。用户会告诉你他们的消费或收入情况。
你需要从用户的输入中提取以下信息，并以JSON格式返回：

{
    "amount": 数字（浮点数），
    "category": "类别（如：餐饮、交通、购物、娱乐、工资、投资等）",
    "type": "收入" 或 "支出",
    "note": "备注说明"
}

示例：
用户：今天午饭花了25块
返回：{"amount": 25.0, "category": "餐饮", "type": "支出", "note": "午饭"}

用户：收到工资8000元
返回：{"amount": 8000.0, "category": "工资", "type": "收入", "note": "工资"}

请只返回JSON，不要有其他文字。"""
    
    // 解析 AI 返回的 JSON
    fun parseAIResponse(response: String): AIAnalysisResult? {
        return try {
            // 尝试提取 JSON 部分
            val jsonStr = extractJson(response)
            val parsed = gson.fromJson(jsonStr, AIJsonResult::class.java)
            
            AIAnalysisResult(
                amount = parsed.amount,
                category = parsed.category,
                type = if (parsed.type == "收入") TransactionType.INCOME else TransactionType.EXPENSE,
                note = parsed.note
            )
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractJson(text: String): String {
        // 尝试找到 JSON 对象
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }
        return text
    }
    
    // 生成用户友好的确认消息
    fun formatConfirmation(result: AIAnalysisResult): String {
        val typeStr = if (result.type == TransactionType.INCOME) "收入" else "支出"
        return """
            |📝 记账确认
            |类型：$typeStr
            |金额：¥${String.format("%.2f", result.amount)}
            |类别：${result.category}
            |备注：${result.note}
        """.trimMargin()
    }
    
    private data class AIJsonResult(
        val amount: Float,
        val category: String,
        val type: String,
        val note: String
    )
}
