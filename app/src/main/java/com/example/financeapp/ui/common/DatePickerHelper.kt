package com.example.financeapp.ui.common

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.utils.StringNames
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar

class DatePickerHelper(
    private val context: Context,
    private var selectedDateMillis: Long? = null,
    private val onDateSelected: (year: Int, month: Int, day: Int, millis: Long) -> Unit
) {
    val styleResId: Int = R.style.CustomThemeOverlay_MaterialCalendar_DatePicker

    fun show() {
        val stringNames = StringNames(context)

        val builder = MaterialDatePicker.Builder.datePicker()

        builder.setTitleText(stringNames.DATE_PICKER_TITLE)
        builder.setTheme(styleResId)

        builder.setSelection(selectedDateMillis ?: DateUtils.todayDateMillis)

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDateMillis = selection

            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selection

            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            onDateSelected(year, month, day, selection)
        }

        picker.show((context as AppCompatActivity).supportFragmentManager, "DATE_PICKER")
    }

    fun setInitialDate(date: Long) {
        selectedDateMillis = date
    }
}