package com.xatcn.privateledger.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.xatcn.privateledger.util.OCRHelper

class TransactionAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "TransactionAccessibility"
        
        // 支付相关的关键字
        private val PAYMENT_KEYWORDS = listOf(
            "支付成功", "付款成功", "转账成功", "收款成功",
            "支付完成", "付款完成", "交易成功"
        )
        
        // 金额相关的关键字
        private val AMOUNT_KEYWORDS = listOf(
            "¥", "￥", "金额", "支付", "付款", "消费"
        )
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                checkForPaymentScreen(event)
            }
        }
    }
    
    override fun onInterrupt() {
        // 服务中断时的处理
    }
    
    private fun checkForPaymentScreen(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // 检查是否是支付成功页面
            val allText = getAllText(rootNode)
            
            if (isPaymentSuccessScreen(allText)) {
                val amount = extractAmount(allText)
                val merchant = extractMerchant(allText)
                
                if (amount != null) {
                    // 发送广播或启动记账界面
                    sendTransactionBroadcast(amount, merchant)
                }
            }
        } finally {
            rootNode.recycle()
        }
    }
    
    private fun getAllText(node: AccessibilityNodeInfo): String {
        val text = StringBuilder()
        
        if (node.text != null) {
            text.append(node.text.toString())
            text.append(" ")
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            text.append(getAllText(child))
            child.recycle()
        }
        
        return text.toString()
    }
    
    private fun isPaymentSuccessScreen(text: String): Boolean {
        return PAYMENT_KEYWORDS.any { text.contains(it) }
    }
    
    private fun extractAmount(text: String): Double? {
        return OCRHelper.extractAmount(text)
    }
    
    private fun extractMerchant(text: String): String? {
        return OCRHelper.extractMerchant(text)
    }
    
    private fun sendTransactionBroadcast(amount: Double, merchant: String?) {
        val intent = Intent("com.xatcn.privateledger.TRANSACTION_DETECTED").apply {
            putExtra("amount", amount)
            putExtra("merchant", merchant ?: "未知商户")
            putExtra("source", "accessibility")
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}
