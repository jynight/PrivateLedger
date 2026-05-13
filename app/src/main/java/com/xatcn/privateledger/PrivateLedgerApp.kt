package com.xatcn.privateledger

import android.app.Application

class PrivateLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PrivateLedgerApp
            private set
    }
}
