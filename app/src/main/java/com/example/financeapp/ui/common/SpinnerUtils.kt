package com.example.financeapp.ui.common

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass

object SpinnerUtils {
    fun setupSpinnerCards(context: Context, spinner: Spinner, data: List<CardsClass>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(R.layout.spinner)
        spinner.adapter = adapter
    }
}