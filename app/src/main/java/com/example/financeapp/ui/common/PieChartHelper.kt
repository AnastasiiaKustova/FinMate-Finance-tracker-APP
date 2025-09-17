package com.example.financeapp.ui.common

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.financeapp.R
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.MoneyFormatter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@SuppressLint("StaticFieldLeak")
object PieChartHelper {

    private var pieChart: PieChart? = null
    private var centerIcon: ImageView? = null
    private var centerText: TextView? = null
    private var categories: List<CategoryClass> = emptyList()
    private var dataList: List<OperationClassWithDate> = emptyList()
    private var money: Int = 0
    private var context: Context? = null

    private var originalColors: List<Int> = emptyList()
    private var categoriesForChart = arrayListOf<CategoryClass>()

    private var onCategorySelected: ((CategoryClass?) -> Unit)? = null

    private var overlay: RadialOverlayView? = null
    private var overlayAnimator: ValueAnimator? = null
    private val ANIM_DURATION = 1000L

    fun setOnCategorySelectedListener(listener: (CategoryClass?) -> Unit) {
        onCategorySelected = listener
    }

    fun setup(
        context: Context,
        pieChart: PieChart,
        overlay: RadialOverlayView,
        centerIcon: ImageView,
        centerText: TextView,
        categories: List<CategoryClass>,
        dataList: List<OperationClassWithDate>,
        money: Int
    ) {
        this.pieChart = pieChart
        this.centerIcon = centerIcon
        this.centerText = centerText
        this.categories = categories
        this.dataList = dataList
        this.money = money
        this.context = context
        this.overlay = overlay

        initChart()
        drawChart()
    }

    fun updateCategories(context: Context, newCategories: List<CategoryClass>) {
        this.categories = newCategories
        this.context = context
        drawChart()
    }

    fun updateDataList(context: Context, newDataList: List<OperationClassWithDate>, newMoney: Int) {
        this.dataList = newDataList
        this.money = newMoney
        this.context = context
        drawChart()
    }

    private fun drawChart(withAnimation: Boolean = true) {
        val chart = pieChart ?: return
        val context = this.context ?: return

        val pieEntries = arrayListOf<PieEntry>()
        val colors = arrayListOf<Int>()
        val usedCategories = arrayListOf<CategoryClass>()

        for (item in dataList) {
            val percent = ((item.info.money?.toFloat() ?: 0f) / money) * 100f
            pieEntries.add(PieEntry(percent))

            val category = categories.find { it.UUID == item.info.categoryUUID }
            category?.let {
                usedCategories.add(category)
                if (it.colorId.isNullOrEmpty())
                    colors.add(ContextCompat.getColor(context, R.color.White))
                else
                    colors.add(Color.parseColor(it.colorId))
            } ?: run {
                usedCategories.add(CategoryClass.empty())
                colors.add(ContextCompat.getColor(context, R.color.White))
            }
        }

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.setColors(colors)

        val pieData = PieData(pieDataSet).apply {
            setDrawValues(false)
        }

        chart.data = pieData
        if (withAnimation) {
            runSynchronizedAnimation()
        } else {
            chart.invalidate()
        }

        chart.centerText = MoneyFormatter.format(money)
        this.categoriesForChart = usedCategories
    }

    private fun initChart() {
        val chart = pieChart ?: return
        val context = this.context ?: return

        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(ContextCompat.getColor(context, R.color.BG))
            holeRadius = 50f
            setTransparentCircleRadius(0f)
            setCenterTextSize(20f)
            setCenterTextColor(ContextCompat.getColor(context, R.color.White))
        }


        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                myOnNothingSelected()
                myOnValueSelected(e, h)
            }

            override fun onNothingSelected() {
                myOnNothingSelected()
            }
        })
    }

    private fun runSynchronizedAnimation() {
        val chart = pieChart ?: return
        val overlay = overlay

        chart.animateXY(ANIM_DURATION.toInt(), ANIM_DURATION.toInt(), Easing.EaseInOutQuad)
        overlay?.innerRatio = chart.holeRadius / 100f

        overlayAnimator?.cancel()
        overlay?.sweepProgress = 0f
        overlayAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIM_DURATION + 200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                overlay?.sweepProgress = va.animatedValue as Float
            }
            start()
        }
    }


    private fun myOnValueSelected(e: Entry?, h: Highlight?) {
        val chart = pieChart ?: return
        val index = h?.x?.toInt() ?: return
        val dataSet = chart.data.getDataSetByIndex(0) as PieDataSet

        val newColors = dataSet.colors.mapIndexed { i, color ->
            if (i == index) color else adjustAlpha(color, 0.3f)
        }
        dataSet.colors = newColors

        val category = categoriesForChart.getOrNull(index)
        chart.centerText = ""

        category?.let {
            onCategorySelected?.invoke(it)
            centerText?.visibility = View.VISIBLE

            centerIcon?.setImageResource(it.iconId)
            centerIcon?.setColorFilter(
                Color.parseColor(it.colorId),
                PorterDuff.Mode.SRC_IN
            )
            centerIcon?.visibility = View.VISIBLE
        }

        chart.invalidate()
    }

    private fun myOnNothingSelected() {
        val chart = pieChart ?: return
        onCategorySelected?.invoke(null)

        drawChart(withAnimation = false)

        chart.centerText = MoneyFormatter.format(money)
        centerIcon?.visibility = View.GONE
        centerText?.visibility = View.GONE
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}