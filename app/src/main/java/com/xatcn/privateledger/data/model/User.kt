package com.xatcn.privateledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Long = 1,
    val username: String,
    val passwordHash: String,
    val nickname: String = "",
    val avatarUri: String? = null,
    val gender: String? = null,
    val signature: String? = null,
    val birthday: String? = null,
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
