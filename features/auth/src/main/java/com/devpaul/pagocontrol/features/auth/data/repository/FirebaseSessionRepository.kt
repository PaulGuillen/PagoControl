package com.devpaul.pagocontrol.features.auth.data.repository

import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.features.auth.data.datasource.AuthRemoteDataSource
import com.devpaul.pagocontrol.features.auth.domain.mapper.toAccountResult
import com.devpaul.pagocontrol.features.auth.domain.mapper.toSignInResult
import com.devpaul.pagocontrol.features.auth.domain.usecase.RegisterUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.SendPasswordResetUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.SignInUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.SignOutUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.UpdateUserNameUseCase
import com.devpaul.pagocontrol.session.AccountRepository
import com.devpaul.pagocontrol.session.AccountResult
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class, AccountRepository::class])
class FirebaseSessionRepository(
    source: AuthRemoteDataSource,
    private val signIn: SignInUseCase,
    private val register: RegisterUseCase,
    private val sendReset: SendPasswordResetUseCase,
    private val updateName: UpdateUserNameUseCase,
    private val signOut: SignOutUseCase,
) : AuthRepository, AccountRepository {
    override val session = source.session
    override suspend fun signIn(email: String, password: String) = when (val result = signIn.invoke(email, password)) {
        is UseCaseResult.Success -> SignInResult.Success
        is UseCaseResult.Error -> result.error.error.toSignInResult()
    }
    override suspend fun register(name: String, email: String, password: String) = when (val result = register.invoke(name, email, password)) {
        is UseCaseResult.Success -> AccountResult.Success
        is UseCaseResult.Error -> result.error.error.toAccountResult()
    }
    override suspend fun sendPasswordReset(email: String) = when (val result = sendReset(email)) {
        is UseCaseResult.Success -> AccountResult.Success
        is UseCaseResult.Error -> result.error.error.toAccountResult()
    }
    override suspend fun updateName(name: String) = when (val result = updateName.invoke(name)) {
        is UseCaseResult.Success -> AccountResult.Success
        is UseCaseResult.Error -> result.error.error.toAccountResult()
    }
    override suspend fun signOut() = signOut.invoke()
}
