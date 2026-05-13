package com.xatcn.privateledger.data.repository

import com.xatcn.privateledger.data.local.TransactionDao
import com.xatcn.privateledger.data.local.CategoryTotal
import com.xatcn.privateledger.data.model.Transaction
import com.xatcn.privateledger.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsByDate(datePrefix: String): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByDate(datePrefix)
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByType(type)
    
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByCategory(category)
    
    suspend fun getTransactionById(id: Long): Transaction? = 
        transactionDao.getTransactionById(id)
    
    suspend fun getTotalByTypeAndDate(type: TransactionType, datePrefix: String): Double = 
        transactionDao.getTotalByTypeAndDate(type, datePrefix) ?: 0.0
    
    suspend fun getCategoryTotals(type: TransactionType, datePrefix: String): List<CategoryTotal> = 
        transactionDao.getCategoryTotals(type, datePrefix)
    
    suspend fun insertTransaction(transaction: Transaction): Long = 
        transactionDao.insertTransaction(transaction)
    
    suspend fun updateTransaction(transaction: Transaction) = 
        transactionDao.updateTransaction(transaction)
    
    suspend fun deleteTransaction(transaction: Transaction) = 
        transactionDao.deleteTransaction(transaction)
    
    suspend fun deleteTransactionById(id: Long) = 
        transactionDao.deleteTransactionById(id)
    
    // 冲销模式：创建反向交易
    suspend fun reverseTransaction(originalId: Long): Transaction? {
        val original = transactionDao.getTransactionById(originalId) ?: return null
        val reversed = original.copy(
            id = 0,
            amount = -original.amount,
            type = if (original.type == TransactionType.INCOME) TransactionType.EXPENSE else TransactionType.INCOME,
            note = "冲销: ${original.note}",
            originalTransactionId = originalId
        )
        val reversedId = transactionDao.insertTransaction(reversed)
        
        // 标记原始交易已被冲销
        transactionDao.updateTransaction(original.copy(isReversed = true, reversedBy = reversedId))
        
        return reversed.copy(id = reversedId)
    }
}
