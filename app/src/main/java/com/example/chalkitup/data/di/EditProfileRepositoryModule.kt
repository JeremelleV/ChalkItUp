package com.example.chalkitup.data.di

import com.example.chalkitup.data.repository.EditProfileRepository
import com.example.chalkitup.domain.repository.EditProfileRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class EditProfileRepositoryModule {
    @Binds
    abstract fun bindEditProfileRepository(
        impl: EditProfileRepository
    ): EditProfileRepositoryInterface
}