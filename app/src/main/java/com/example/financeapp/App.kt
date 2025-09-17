package com.example.financeapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App: Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        instance = this
    }
}