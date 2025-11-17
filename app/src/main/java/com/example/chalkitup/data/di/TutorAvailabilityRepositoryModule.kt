package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.TutorAvailabilityRepository
import com.example.chalkitup.domain.repository.TutorAvailabilityRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TutorAvailabilityRepositoryModule {
    @Binds
    abstract fun bindTutorAvailabilityRepository(
        impl: TutorAvailabilityRepository
    ): TutorAvailabilityRepositoryInterface
}