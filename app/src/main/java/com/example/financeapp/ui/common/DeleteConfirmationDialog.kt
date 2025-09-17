package com.example.financeapp.ui.common

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.financeapp.R
import com.example.financeapp.utils.StringNames

object DeleteConfirmationDialog {
    fun show(
        context: Context,
        onConfirm: () -> Unit
    ) {
        val stringNames = StringNames(context)

        val dialog = AlertDialog.Builder(context)
            .setTitle(stringNames.DELETE_TITLE)
            .setMessage(stringNames.DELETE_MESSAGE)
            .setPositiveButton(stringNames.YES_TITLE) { _, _ -> onConfirm() }
            .setNegativeButton(stringNames.CANCEL_TITLE, null)
            .create()

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.rounded_shape)
        )

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(context, R.color.Red))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(context, R.color.White))
        }

        dialog.show()
    }
}