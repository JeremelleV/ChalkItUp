package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.SettingsRepository
import com.example.chalkitup.domain.repository.SettingsRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {
    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepository
    ): SettingsRepositoryInterface
}