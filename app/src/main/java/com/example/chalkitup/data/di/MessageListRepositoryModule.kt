package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.MessageListRepository
import com.example.chalkitup.domain.repository.MessageListRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageListRepositoryModule {
    @Binds
    abstract fun bindMessageListRepository(
        impl: MessageListRepository
    ): MessageListRepositoryInterface
}