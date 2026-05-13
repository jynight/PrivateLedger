package com.xatcn.privateledger.data.local

import androidx.room.*
import com.xatcn.privateledger.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    fun getCurrentUser(): Flow<User?>
    
    @Query("SELECT * FROM users WHERE id = 1")
    suspend fun getCurrentUserSync(): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("DELETE FROM users WHERE id = 1")
    suspend fun deleteCurrentUser()
}
