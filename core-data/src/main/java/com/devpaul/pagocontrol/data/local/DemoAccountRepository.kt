package com.devpaul.pagocontrol.data.local

import com.devpaul.pagocontrol.session.AccountRepository
import com.devpaul.pagocontrol.session.AccountResult

class DemoAccountRepository : AccountRepository {
    override suspend fun register(name: String, email: String, password: String) = AccountResult.DemoOnly
    override suspend fun sendPasswordReset(email: String) = AccountResult.DemoOnly
}
