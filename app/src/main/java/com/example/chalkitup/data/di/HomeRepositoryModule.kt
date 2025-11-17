package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.HomeRepository
import com.example.chalkitup.domain.repository.HomeRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRepositoryModule {
    @Binds
    abstract fun bindHomeRepository(
        impl: HomeRepository
    ): HomeRepositoryInterface
}