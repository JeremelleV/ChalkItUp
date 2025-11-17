package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.AndroidEmailValidator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.chalkitup.domain.repository.AuthRepositoryInterface
import com.example.chalkitup.data.repository.AuthRepository
import com.example.chalkitup.domain.repository.EmailValidator
import dagger.Provides

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {
    @Binds
    abstract fun bindAuthRepository(
        authRepository: AuthRepository
    ): AuthRepositoryInterface
}

@Module
@InstallIn(SingletonComponent::class)
object ValidationModule {
    @Provides
    fun provideEmailValidator(): EmailValidator = AndroidEmailValidator()
}

