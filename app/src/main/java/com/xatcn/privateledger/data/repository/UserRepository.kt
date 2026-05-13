package com.xatcn.privateledger.data.repository

import com.xatcn.privateledger.data.local.UserDao
import com.xatcn.privateledger.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()
    
    suspend fun getCurrentUserSync(): User? = userDao.getCurrentUserSync()
    
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    suspend fun deleteCurrentUser() = userDao.deleteCurrentUser()
}
