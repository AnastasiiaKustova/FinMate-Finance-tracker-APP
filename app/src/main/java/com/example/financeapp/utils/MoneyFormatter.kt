package com.example.financeapp.utils

import com.example.financeapp.ui.common.CurrencyManager
import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {

    fun format(amount: Int): String {
        val currencySymbol = CurrencyManager.getCurrency()
         return try {
            val formatter = NumberFormat.getInstance(Locale.getDefault())
            formatter.isGroupingUsed = true
            formatter.maximumFractionDigits = 0
            val formatted = formatter.format(amount)
                .replace(" ", "\u202f")
            "$formatted $currencySymbol"
        } catch (e: Exception) {
            "0 $currencySymbol"
        }
    }
}