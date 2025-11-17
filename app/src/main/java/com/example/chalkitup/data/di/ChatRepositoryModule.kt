package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.ChatRepository
import com.example.chalkitup.domain.repository.ChatRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatRepositoryModule {
    @Binds
    abstract fun bindChatRepository(
        impl: ChatRepository
    ): ChatRepositoryInterface
}
