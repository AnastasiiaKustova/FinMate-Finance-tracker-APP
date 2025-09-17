package com.example.financeapp.ui.common

import android.content.Context
import android.widget.Toast

object ToastHelper {
    fun show(
        context: Context,
        message: String
    ){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}