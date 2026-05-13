package com.xatcn.privateledger.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val displayFormatter = DateTimeFormatter.ofPattern("MM月dd日 HH:mm")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy年")
    
    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(dateFormatter)
    }
    
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
    
    fun formatForDisplay(dateTime: LocalDateTime): String {
        return dateTime.format(displayFormatter)
    }
    
    fun formatMonth(dateTime: LocalDateTime): String {
        return dateTime.format(monthFormatter)
    }
    
    fun formatYear(dateTime: LocalDateTime): String {
        return dateTime.format(yearFormatter)
    }
    
    fun getCurrentDatePrefix(): String {
        return LocalDateTime.now().format(dateFormatter).substring(0, 7) // yyyy-MM
    }
    
    fun getCurrentYearPrefix(): String {
        return LocalDateTime.now().format(dateFormatter).substring(0, 4) // yyyy
    }
    
    fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return LocalDateTime.parse(dateTimeStr, dateTimeFormatter)
    }
}
