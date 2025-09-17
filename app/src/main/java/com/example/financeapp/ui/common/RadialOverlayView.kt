package com.example.financeapp.ui.common

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RadialOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var sweepProgress: Float = 0f
        set(value) { field = value; invalidate() }

    var innerRatio: Float = 0.5f

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val outerRadius = width.coerceAtMost(height) / 2f
        val innerRadius = outerRadius * innerRatio - 8f

        val sweepAngle = 360f * sweepProgress

        val rect = RectF(
            cx - outerRadius,
            cy - outerRadius,
            cx + outerRadius,
            cy + outerRadius
        )

        val shader = RadialGradient(
            cx, cy, outerRadius,
            intArrayOf(Color.argb(50, 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = shader

        val checkpoint = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        canvas.drawArc(rect, -90f, sweepAngle, true, gradientPaint)
        canvas.drawCircle(cx, cy, innerRadius, clearPaint)
        canvas.restoreToCount(checkpoint)
    }
}
