package com.xatcn.privateledger.data.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    @Test
    fun `创建用户对象 - 基本信息`() {
        val user = User(
            username = "testuser",
            passwordHash = "abc123hash"
        )

        assertEquals(1L, user.id)
        assertEquals("testuser", user.username)
        assertEquals("abc123hash", user.passwordHash)
        assertEquals("", user.nickname)
        assertNull(user.avatarUri)
        assertNull(user.gender)
        assertNull(user.signature)
        assertNull(user.birthday)
    }

    @Test
    fun `创建用户对象 - 完整信息`() {
        val user = User(
            username = "testuser",
            passwordHash = "abc123hash",
            nickname = "测试用户",
            avatarUri = "/path/to/avatar.jpg",
            gender = "男",
            signature = "这是签名",
            birthday = "2000-01-01"
        )

        assertEquals("测试用户", user.nickname)
        assertEquals("/path/to/avatar.jpg", user.avatarUri)
        assertEquals("男", user.gender)
        assertEquals("这是签名", user.signature)
        assertEquals("2000-01-01", user.birthday)
    }

    @Test
    fun `复制用户对象 - 更新昵称`() {
        val user = User(
            username = "testuser",
            passwordHash = "abc123hash"
        )

        val updated = user.copy(nickname = "新昵称")

        assertEquals("testuser", updated.username)
        assertEquals("新昵称", updated.nickname)
    }
}
