package com.xatcn.privateledger.service

import android.content.Context
import android.net.Uri
import com.xatcn.privateledger.data.model.AIAnalysisResult
import com.xatcn.privateledger.data.model.TransactionType
import com.xatcn.privateledger.util.AIHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AIService(private val context: Context) {
    
    private var isModelLoaded = false
    
    // 模型文件目录
    private val modelDir: File
        get() = File(context.filesDir, "models").apply { mkdirs() }
    
    // 获取已导入的模型文件
    fun getModelFiles(): List<File> {
        return modelDir.listFiles()?.filter { 
            it.extension in listOf("bin", "tflite", "task") 
        } ?: emptyList()
    }
    
    // 检查是否有可用模型
    fun hasModel(): Boolean {
        return getModelFiles().isNotEmpty()
    }
    
    // 获取模型文件路径
    fun getModelPath(): String? {
        return getModelFiles().firstOrNull()?.absolutePath
    }
    
    // 从 Uri 导入模型文件
    suspend fun importModel(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("无法读取文件"))
            
            // 获取文件名
            val fileName = getFileNameFromUri(uri)
            val outputFile = File(modelDir, fileName)
            
            // 复制文件
            FileOutputStream(outputFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            // 释放旧模型
            releaseModel()
            
            Result.success("模型导入成功：$fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 加载模型
    suspend fun loadModel(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelPath = getModelPath()
                ?: return@withContext Result.failure(Exception("没有可用的模型文件"))
            
            // 释放旧模型
            releaseModel()
            
            // 注意：MediaPipe LLM Inference API 需要特定的模型格式
            // 这里只是标记模型已加载，实际推理需要根据模型格式实现
            isModelLoaded = true
            
            Result.success("模型加载成功")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 使用 AI 解析记账信息
    suspend fun analyzeTransaction(userInput: String): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // 使用规则解析（作为主要方案，因为 MediaPipe API 需要特定模型格式）
            return@withContext parseWithRules(userInput)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    // 使用规则解析
    private fun parseWithRules(userInput: String): Result<AIAnalysisResult> {
        try {
            // 提取金额
            val amountPatterns = listOf(
                Regex("""(\d+\.?\d*)\s*[元块]"""),
                Regex("""[¥￥]\s*(\d+\.?\d*)"""),
                Regex("""花了\s*(\d+\.?\d*)"""),
                Regex("""收到\s*(\d+\.?\d*)"""),
                Regex("""(\d+\.?\d*)""")
            )
            
            var amount = 0f
            for (pattern in amountPatterns) {
                val match = pattern.find(userInput)
                if (match != null) {
                    amount = match.groupValues[1].toFloatOrNull() ?: 0f
                    if (amount > 0) break
                }
            }
            
            if (amount <= 0) {
                return Result.failure(Exception("无法识别金额"))
            }
            
            // 判断类型
            val incomeKeywords = listOf("收到", "收入", "工资", "奖金", "红包", "转账收")
            val isIncome = incomeKeywords.any { userInput.contains(it) }
            
            // 识别类别
            val categoryMap = mapOf(
                "餐饮" to listOf("饭", "餐", "吃", "外卖", "奶茶", "咖啡", "小吃"),
                "交通" to listOf("打车", "地铁", "公交", "出租", "滴滴", "高铁", "飞机"),
                "购物" to listOf("买", "购", "淘宝", "京东", "拼多多", "商场"),
                "娱乐" to listOf("电影", "游戏", "KTV", "旅游", "景点"),
                "居住" to listOf("房租", "水电", "物业", "燃气"),
                "通讯" to listOf("话费", "流量", "宽带"),
                "医疗" to listOf("医院", "药", "看病", "挂号"),
                "教育" to listOf("书", "课程", "培训", "学费"),
                "工资" to listOf("工资", "薪水", "薪资"),
                "奖金" to listOf("奖金", "年终", "绩效"),
                "红包" to listOf("红包", "转账")
            )
            
            var category = "其他"
            for ((cat, keywords) in categoryMap) {
                if (keywords.any { userInput.contains(it) }) {
                    category = cat
                    break
                }
            }
            
            // 提取备注
            val note = userInput.replace(Regex("""\d+\.?\d*\s*[元块]?"""), "").trim()
            
            return Result.success(
                AIAnalysisResult(
                    amount = amount,
                    category = category,
                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                    note = note.ifBlank { category },
                    confidence = 0.7f
                )
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    // 释放模型
    fun releaseModel() {
        isModelLoaded = false
    }
    
    // 删除模型文件
    fun deleteModel(fileName: String): Boolean {
        val file = File(modelDir, fileName)
        return file.delete()
    }
    
    // 从 Uri 获取文件名
    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "model_${System.currentTimeMillis()}.bin"
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AIService? = null
        
        fun getInstance(context: Context): AIService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
