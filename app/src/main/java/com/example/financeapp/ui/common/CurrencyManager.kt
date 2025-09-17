package com.example.financeapp.ui.common

import android.content.Context
import java.util.Currency
import java.util.Locale

object CurrencyManager {

    private const val PREFS_NAME = "currency_prefs"
    private const val KEY_CURRENCY = "currency"

    private var curCurrency: String? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        curCurrency = prefs.getString(KEY_CURRENCY, null)
    }

    fun getCurrency(): String {
        val currencyCode = getDefaultCurrencyCode()
        return curCurrency ?: getCurrencyByCode(currencyCode)
    }

    private fun getDefaultCurrencyCode(): String {
        return try {
            val country = Locale.getDefault().country
            if (country.isNullOrEmpty()) "USD"
            else Currency.getInstance(Locale("", country)).currencyCode
        } catch (e: Exception) {
            "USD"
        }
    }

    fun getCurrencyCode(): String{
        return when (curCurrency) {
            "$" -> "USD"
            "€" -> "EUR"
            "₽" -> "RUB"
            "Br" -> "BYN"
            "₴" -> "UAH"
            "₸" -> "KZT"
            else -> "USD"
        }
    }

    private fun getCurrencyByCode(currencyCode: String): String{
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "RUB" -> "₽"
            "BYN" -> "Br"
            "UAH" -> "₴"
            "KZT" -> "₸"
            else -> "$"
        }
    }

    fun changeCurrencyByCode(context: Context, currencyCode: String){
        curCurrency = getCurrencyByCode(currencyCode)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, curCurrency).apply()
    }
}
