package com.example.financeapp.ui.common

import android.content.Context
import androidx.core.content.ContextCompat
import com.db.williamchart.data.AxisType
import com.db.williamchart.view.BarChartView
import com.example.financeapp.R
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.utils.Months

class BarChartHelper(){

    private val months = Months.getShortNameOfMonth()

    fun setup(barChart : BarChartView, dataList : List<OperationClassWithDate>) {
        val barEntries = arrayListOf<Pair<String, Float>>()

        for (month in months) {
            val data = dataList.find { it.date == month.key }
            barEntries.add(Pair(month.value, data?.info?.money?.toFloat() ?: 0f))
        }

        barChart.apply {
            animate(barEntries)
            barsColor = ContextCompat.getColor(context, R.color.Primary)
            labelsColor = ContextCompat.getColor(context, R.color.White60)
            barRadius = 16f
            axis = AxisType.X
            spacing = 20f
            labelsSize = 40f
        }

    }
}