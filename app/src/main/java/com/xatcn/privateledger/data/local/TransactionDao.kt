package com.xatcn.privateledger.data.local

import androidx.room.*
import com.xatcn.privateledger.data.model.Transaction
import com.xatcn.privateledger.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE date LIKE :datePrefix || '%' ORDER BY date DESC")
    fun getTransactionsByDate(datePrefix: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date LIKE :datePrefix || '%'")
    suspend fun getTotalByTypeAndDate(type: TransactionType, datePrefix: String): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type AND date LIKE :datePrefix || '%' GROUP BY category ORDER BY total DESC")
    suspend fun getCategoryTotals(type: TransactionType, datePrefix: String): List<CategoryTotal>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
