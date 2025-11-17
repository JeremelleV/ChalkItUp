package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.CertificationRepository
import com.example.chalkitup.domain.repository.CertificationRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CertificationRepositoryModule {
    @Binds
    abstract fun bindCertificationRepository(
        impl: CertificationRepository
    ): CertificationRepositoryInterface
}