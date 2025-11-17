package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.BookingRepository
import com.example.chalkitup.domain.repository.BookingRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingRepositoryModule {
    @Binds
    abstract fun bindBookingRepository(
        impl: BookingRepository
    ): BookingRepositoryInterface
}