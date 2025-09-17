package com.example.financeapp.ui.common

import android.view.GestureDetector
import android.view.MotionEvent
import com.db.williamchart.view.BarChartView

class BarChartClickHelper(
    private val barChart: BarChartView,
    private val barCount: Int,
    private val onBarClick: (index: Int) -> Unit
) {

    private val gestureDetector = GestureDetector(
        barChart.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val chartWidth = barChart.width.toFloat()
                val slotWidth = chartWidth / barCount

                val index = (e.x / slotWidth).toInt().coerceIn(0, barCount - 1)

                onBarClick(index)
                return true
            }
        }
    )

    init {
        barChart.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }
}