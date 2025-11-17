package com.example.chalkitup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.repository.SettingsRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepositoryInterface
) : ViewModel() {

    fun getEmail(): String? = settingsRepository.getEmail()

    fun deleteAccount(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.deleteAccount().fold(
                onSuccess = { onSuccess() },
                onFailure = { onError() }
            )
        }
    }
}
