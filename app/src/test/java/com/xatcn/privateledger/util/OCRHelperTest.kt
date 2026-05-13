package com.xatcn.privateledger.util

import org.junit.Assert.*
import org.junit.Test

class OCRHelperTest {

    @Test
    fun `提取金额 - 人民币符号`() {
        assertEquals(25.0, OCRHelper.extractAmount("¥25.00")!!, 0.01)
        assertEquals(100.5, OCRHelper.extractAmount("￥100.5")!!, 0.01)
    }

    @Test
    fun `提取金额 - 元或块`() {
        assertEquals(25.0, OCRHelper.extractAmount("25元")!!, 0.01)
        assertEquals(30.0, OCRHelper.extractAmount("30块")!!, 0.01)
        assertEquals(15.5, OCRHelper.extractAmount("15.5元")!!, 0.01)
    }

    @Test
    fun `提取金额 - 关键字前缀`() {
        assertEquals(100.0, OCRHelper.extractAmount("金额：100")!!, 0.01)
        assertEquals(200.0, OCRHelper.extractAmount("支付：200")!!, 0.01)
        assertEquals(300.0, OCRHelper.extractAmount("消费：300")!!, 0.01)
    }

    @Test
    fun `提取金额 - 纯数字`() {
        assertEquals(50.0, OCRHelper.extractAmount("50")!!, 0.01)
        assertEquals(99.99, OCRHelper.extractAmount("99.99")!!, 0.01)
    }

    @Test
    fun `提取金额 - 无金额`() {
        assertNull(OCRHelper.extractAmount("没有金额信息"))
        assertNull(OCRHelper.extractAmount(""))
    }

    @Test
    fun `提取商户 - 商户关键字`() {
        assertEquals("星巴克", OCRHelper.extractMerchant("商户：星巴克"))
        assertEquals("麦当劳", OCRHelper.extractMerchant("商家：麦当劳"))
    }

    @Test
    fun `提取商户 - 收款方关键字`() {
        assertEquals("淘宝店铺", OCRHelper.extractMerchant("收款方：淘宝店铺"))
        assertEquals("张三", OCRHelper.extractMerchant("付款给：张三"))
    }

    @Test
    fun `提取商户 - 无商户信息`() {
        assertNull(OCRHelper.extractMerchant("没有商户信息"))
        assertNull(OCRHelper.extractMerchant(""))
    }
}
