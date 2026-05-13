package com.xatcn.privateledger.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class DateUtilsTest {

    @Test
    fun `格式化日期 - yyyy-MM-dd`() {
        val date = LocalDateTime.of(2026, 5, 13, 14, 30, 0)
        assertEquals("2026-05-13", DateUtils.formatDate(date))
    }

    @Test
    fun `格式化日期时间 - yyyy-MM-dd HH-mm-ss`() {
        val date = LocalDateTime.of(2026, 5, 13, 14, 30, 45)
        assertEquals("2026-05-13 14:30:45", DateUtils.formatDateTime(date))
    }

    @Test
    fun `格式化显示 - MM月dd日 HH-mm`() {
        val date = LocalDateTime.of(2026, 5, 13, 14, 30, 0)
        assertEquals("05月13日 14:30", DateUtils.formatForDisplay(date))
    }

    @Test
    fun `格式化月份 - yyyy年MM月`() {
        val date = LocalDateTime.of(2026, 5, 13, 14, 30, 0)
        assertEquals("2026年05月", DateUtils.formatMonth(date))
    }

    @Test
    fun `格式化年份 - yyyy年`() {
        val date = LocalDateTime.of(2026, 5, 13, 14, 30, 0)
        assertEquals("2026年", DateUtils.formatYear(date))
    }

    @Test
    fun `获取当前日期前缀 - yyyy-MM`() {
        val prefix = DateUtils.getCurrentDatePrefix()
        assertTrue(prefix.matches(Regex("""\d{4}-\d{2}""")))
    }

    @Test
    fun `获取当前年份前缀 - yyyy`() {
        val prefix = DateUtils.getCurrentYearPrefix()
        assertTrue(prefix.matches(Regex("""\d{4}""")))
    }

    @Test
    fun `解析日期时间`() {
        val dateStr = "2026-05-13 14:30:45"
        val date = DateUtils.parseDateTime(dateStr)

        assertEquals(2026, date.year)
        assertEquals(5, date.monthValue)
        assertEquals(13, date.dayOfMonth)
        assertEquals(14, date.hour)
        assertEquals(30, date.minute)
        assertEquals(45, date.second)
    }
}
