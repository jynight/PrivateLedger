package com.xatcn.privateledger

import android.app.Application
import com.xatcn.privateledger.data.local.AppDatabase
import com.xatcn.privateledger.data.repository.TransactionRepository
import com.xatcn.privateledger.data.repository.UserRepository
import com.xatcn.privateledger.data.repository.SecurityRepository

class PrivateLedgerApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var transactionRepository: TransactionRepository
        private set
    lateinit var userRepository: UserRepository
        private set
    lateinit var securityRepository: SecurityRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化安全仓库（管理数据库密码）
        securityRepository = SecurityRepository(this)

        // 使用安全密码初始化加密数据库
        val passphrase = securityRepository.getDatabasePassphrase()
        database = AppDatabase.getDatabase(this, passphrase)

        // 初始化数据仓库
        transactionRepository = TransactionRepository(database.transactionDao())
        userRepository = UserRepository(database.userDao())
    }

    companion object {
        lateinit var instance: PrivateLedgerApp
            private set
    }
}
