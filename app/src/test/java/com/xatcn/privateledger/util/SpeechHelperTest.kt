package com.xatcn.privateledger.util

import org.junit.Assert.*
import org.junit.Test

class SpeechHelperTest {

    @Test
    fun `语音状态 - 初始状态`() {
        // 测试状态枚举
        val idle = SpeechState.Idle
        val listening = SpeechState.Listening
        val processing = SpeechState.Processing
        val success = SpeechState.Success("测试")
        val error = SpeechState.Error("错误")

        assertTrue(idle is SpeechState.Idle)
        assertTrue(listening is SpeechState.Listening)
        assertTrue(processing is SpeechState.Processing)
        assertTrue(success is SpeechState.Success)
        assertTrue(error is SpeechState.Error)
    }

    @Test
    fun `语音状态 - 成功结果`() {
        val success = SpeechState.Success("今天午饭花了25块")
        assertEquals("今天午饭花了25块", success.text)
    }

    @Test
    fun `语音状态 - 错误消息`() {
        val error = SpeechState.Error("网络错误")
        assertEquals("网络错误", error.message)
    }
}
