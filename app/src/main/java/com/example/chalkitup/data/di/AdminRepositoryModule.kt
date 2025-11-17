package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.AdminRepository
import com.example.chalkitup.domain.repository.AdminRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminRepositoryModule {
    @Binds
    abstract fun bindAdminRepository(
        impl: AdminRepository
    ): AdminRepositoryInterface
}