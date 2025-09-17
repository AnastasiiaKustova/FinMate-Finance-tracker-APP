package com.example.financeapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financeapp.useCase.settings.Settings

class SettingsViewModel: ViewModel() {

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings> = _settings

    fun updateSettings(settings: Settings) {
        _settings.value = settings
    }

    fun initIfEmpty(settings: Settings) {
        if (_settings.value == null) {
            _settings.value = settings
        }
    }
}