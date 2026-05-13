package com.xatcn.privateledger.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xatcn.privateledger.data.local.AppDatabase
import com.xatcn.privateledger.data.model.Transaction
import com.xatcn.privateledger.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupService(private val context: Context) {
    
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context, "backup_temp_key")
    }
    
    // 备份数据结构
    data class BackupData(
        val version: Int = 1,
        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        val transactions: List<Transaction>,
        val user: User?
    )
    
    // 导出到 SAF 目录
    suspend fun exportToSaf(directoryUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, directoryUri)
                ?: return@withContext Result.failure(Exception("无法访问目录"))
            
            // 获取所有交易数据
            val transactions = database.transactionDao().getAllTransactions().first()
            val user = database.userDao().getCurrentUserSync()
            
            // 创建备份数据
            val backupData = BackupData(
                transactions = transactions,
                user = user
            )
            
            // 生成文件名
            val fileName = "private_ledger_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            
            // 创建文件
            val file = documentFile.createFile("application/json", fileName)
                ?: return@withContext Result.failure(Exception("无法创建文件"))
            
            // 写入数据
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                val json = gson.toJson(backupData)
                outputStream.write(json.toByteArray())
            }
            
            Result.success("备份成功：$fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 从 SAF 导入
    suspend fun importFromSaf(fileUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return@withContext Result.failure(Exception("无法读取文件"))
            
            val json = inputStream.bufferedReader().use { it.readText() }
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            // 清空现有数据并导入
            // 注意：这里需要根据实际需求决定是合并还是覆盖
            backupData.transactions.forEach { transaction ->
                database.transactionDao().insertTransaction(transaction.copy(id = 0))
            }
            
            if (backupData.user != null) {
                database.userDao().insertUser(backupData.user)
            }
            
            Result.success("导入成功：${backupData.transactions.size} 条记录")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 自动备份到公共目录
    suspend fun autoBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 检查存储权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用 MediaStore 或 SAF
                return@withContext Result.failure(Exception("请使用手动备份到 SAF 目录"))
            }
            
            // 获取公共目录
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val backupDir = File(publicDir, "PrivateLedger").apply { mkdirs() }
            
            // 获取所有交易数据
            val transactions = database.transactionDao().getAllTransactions().first()
            val user = database.userDao().getCurrentUserSync()
            
            // 创建备份数据
            val backupData = BackupData(
                transactions = transactions,
                user = user
            )
            
            // 生成文件名
            val fileName = "private_ledger_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val file = File(backupDir, fileName)
            
            // 写入数据
            FileOutputStream(file).use { outputStream ->
                val json = gson.toJson(backupData)
                outputStream.write(json.toByteArray())
            }
            
            Result.success("自动备份成功：${file.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 从本地文件导入
    suspend fun importFromFile(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = file.readText()
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            // 导入数据
            backupData.transactions.forEach { transaction ->
                database.transactionDao().insertTransaction(transaction.copy(id = 0))
            }
            
            if (backupData.user != null) {
                database.userDao().insertUser(backupData.user)
            }
            
            Result.success("导入成功：${backupData.transactions.size} 条记录")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 获取备份文件列表
    fun getBackupFiles(): List<File> {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val backupDir = File(publicDir, "PrivateLedger")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    // 删除备份文件
    fun deleteBackupFile(file: File): Boolean {
        return file.delete()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: BackupService? = null
        
        fun getInstance(context: Context): BackupService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackupService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
