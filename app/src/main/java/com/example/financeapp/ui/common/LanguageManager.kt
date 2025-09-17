package com.example.financeapp.ui.common

import android.content.Context
import com.example.financeapp.utils.Constance.DEFAULT
import java.util.Locale


object LanguageManager {
    private const val KEY_LANGUAGE = "key_language"

    // кэш языка в памяти
    @Volatile private var cachedLanguage: String? = null

    fun init(context: Context) {
        cachedLanguage = getPrefs(context).getString(KEY_LANGUAGE, DEFAULT) ?: DEFAULT
        val code = getLanguage()
        if (code == DEFAULT) return  // ничего не меняем, система сама
        applyLanguage(context, code)
    }

    private fun applyLanguage(context: Context, lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun setLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
        cachedLanguage = languageCode
    }

    fun getLanguage(): String {
        return cachedLanguage ?: DEFAULT
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
}