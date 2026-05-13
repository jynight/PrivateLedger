package com.xatcn.privateledger.data.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class TransactionTest {

    @Test
    fun `创建交易对象 - 默认值`() {
        val transaction = Transaction(
            amount = 100.0,
            category = "餐饮",
            type = TransactionType.EXPENSE
        )

        assertEquals(0L, transaction.id)
        assertEquals(100.0, transaction.amount, 0.01)
        assertEquals("餐饮", transaction.category)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals("", transaction.note)
        assertFalse(transaction.isReversed)
        assertNull(transaction.reversedBy)
        assertNull(transaction.originalTransactionId)
    }

    @Test
    fun `创建交易对象 - 收入类型`() {
        val transaction = Transaction(
            amount = 5000.0,
            category = "工资",
            type = TransactionType.INCOME,
            note = "月薪"
        )

        assertEquals(5000.0, transaction.amount, 0.01)
        assertEquals("工资", transaction.category)
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals("月薪", transaction.note)
    }

    @Test
    fun `创建交易对象 - 冲销记录`() {
        val transaction = Transaction(
            amount = -100.0,
            category = "餐饮",
            type = TransactionType.EXPENSE,
            note = "冲销: 午餐",
            originalTransactionId = 1L
        )

        assertEquals(-100.0, transaction.amount, 0.01)
        assertEquals("冲销: 午餐", transaction.note)
        assertEquals(1L, transaction.originalTransactionId)
    }

    @Test
    fun `交易类型枚举 - 包含所有类型`() {
        val types = TransactionType.values()
        assertEquals(2, types.size)
        assertTrue(types.contains(TransactionType.INCOME))
        assertTrue(types.contains(TransactionType.EXPENSE))
    }

    @Test
    fun `交易来源枚举 - 包含所有来源`() {
        val sources = TransactionSource.values()
        assertEquals(4, sources.size)
        assertTrue(sources.contains(TransactionSource.MANUAL))
        assertTrue(sources.contains(TransactionSource.AI_CHAT))
        assertTrue(sources.contains(TransactionSource.OCR))
        assertTrue(sources.contains(TransactionSource.ACCESSIBILITY))
    }

    @Test
    fun `复制交易对象 - 修改金额`() {
        val original = Transaction(
            id = 1L,
            amount = 100.0,
            category = "餐饮",
            type = TransactionType.EXPENSE
        )

        val copied = original.copy(amount = 200.0)

        assertEquals(1L, copied.id)
        assertEquals(200.0, copied.amount, 0.01)
        assertEquals("餐饮", copied.category)
    }

    @Test
    fun `复制交易对象 - 创建冲销记录`() {
        val original = Transaction(
            id = 1L,
            amount = 100.0,
            category = "餐饮",
            type = TransactionType.EXPENSE
        )

        val reversed = original.copy(
            id = 0,
            amount = -original.amount,
            type = TransactionType.INCOME,
            note = "冲销: ${original.note}",
            originalTransactionId = original.id
        )

        assertEquals(0L, reversed.id)
        assertEquals(-100.0, reversed.amount, 0.01)
        assertEquals(TransactionType.INCOME, reversed.type)
        assertEquals(1L, reversed.originalTransactionId)
    }
}
