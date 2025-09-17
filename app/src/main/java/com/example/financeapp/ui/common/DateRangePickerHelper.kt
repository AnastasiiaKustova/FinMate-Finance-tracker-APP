package com.example.financeapp.ui.common

import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*
import androidx.fragment.app.FragmentManager
import com.example.financeapp.R
import com.example.financeapp.utils.DateUtils
import androidx.core.util.Pair

class DateRangePickerHelper(
    private val fragmentManager: FragmentManager,
    private val startDateMillis: Long?,
    private val endDateMillis: Long?,
    private val onRangeSelected: (
        startDate: String,
        endDate: String,
        startDateMillis: Long,
        endDateMillis: Long) -> Unit
) {

    fun show() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.CustomThemeOverlay_MaterialCalendar)

        val startDate: Long = startDateMillis ?: DateUtils.startOfMonthDateMillis
        val endDate: Long = endDateMillis ?: DateUtils.endOfMonthDateMillis

        builder.setSelection(Pair(startDate, endDate))

        val dateRangePicker = builder.build()

        dateRangePicker.show(fragmentManager, "dateRangePicker")

        dateRangePicker.addOnPositiveButtonClickListener { pickedRange ->
            val startMillis = pickedRange.first
            val endMillis = pickedRange.second

            val calendarStart = Calendar.getInstance().apply { timeInMillis = startMillis }
            val calendarEnd = Calendar.getInstance().apply { timeInMillis = endMillis }

            val start = DateUtils.parseMillisToString(calendarStart)
            val end = DateUtils.parseMillisToString(calendarEnd)

            onRangeSelected(start, end, startMillis, endMillis)
        }
    }
}