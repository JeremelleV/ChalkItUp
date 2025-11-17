package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.ProfileRepository
import com.example.chalkitup.domain.repository.ProfileRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {
    @Binds
    abstract fun bindProfileRepository(
        impl: ProfileRepository
    ): ProfileRepositoryInterface
}