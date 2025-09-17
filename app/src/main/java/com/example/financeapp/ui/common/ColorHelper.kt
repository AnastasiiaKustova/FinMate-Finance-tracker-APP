package com.example.financeapp.ui.common

import android.graphics.Color
import kotlin.random.Random

object ColorHelper {
    fun createRandomColor(): Int {
        val r = Random.nextInt(0, 256)
        val g = Random.nextInt(0, 256)
        val b = Random.nextInt(0, 256)
        return Color.rgb(r, g, b)
    }

    /** Сохранение в HEX (#RRGGBB) */
    fun toHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    /** Восстановление из HEX */
    fun fromHex(hex: String): Int {
        return Color.parseColor(hex)
    }
}