package com.xatcn.privateledger.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OCRHelper {
    
    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    
    // 从 Uri 识别文字
    suspend fun recognizeText(context: Context, imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    // 从文字中提取金额
    fun extractAmount(text: String): Double? {
        // 匹配常见金额格式
        val patterns = listOf(
            Regex("""[¥￥]\s*(\d+\.?\d*)"""),           // ¥25.00
            Regex("""(\d+\.?\d*)\s*[元块]"""),          // 25元, 25块
            Regex("""金额[：:]\s*(\d+\.?\d*)"""),       // 金额：25.00
            Regex("""支付[：:]\s*(\d+\.?\d*)"""),       // 支付：25.00
            Regex("""消费[：:]\s*(\d+\.?\d*)"""),       // 消费：25.00
            Regex("""(\d+\.?\d*)""")                    // 纯数字
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amount = match.groupValues[1].toDoubleOrNull()
                if (amount != null && amount > 0) {
                    return amount
                }
            }
        }
        return null
    }
    
    // 从文字中提取商户名
    fun extractMerchant(text: String): String? {
        val patterns = listOf(
            Regex("""商户[：:]\s*(.+)"""),
            Regex("""商家[：:]\s*(.+)"""),
            Regex("""收款方[：:]\s*(.+)"""),
            Regex("""付款给[：:]\s*(.+)""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
}
