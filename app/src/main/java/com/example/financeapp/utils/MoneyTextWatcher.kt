package com.example.financeapp.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Locale

class MoneyTextWatcher(
    private val editText: EditText,
) : TextWatcher {

    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            try {
                // убираем всё кроме цифр
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                if (cleanString.isNotEmpty()) {
                    val parsed = cleanString.toLong()

                    // форматируем с разделителем тысяч
                    val formatted = NumberFormat
                        .getNumberInstance(Locale("ru", "RU"))
                        .format(parsed)

                    current = "$formatted"
                    editText.setText(current)
                    editText.setSelection(current.length) // курсор в конец
                } else {
                    current = ""
                    editText.setText("")
                }
            } catch (e: Exception) {
                current = "0"
                editText.setText(current)
                editText.setSelection(current.length)
            }

            editText.addTextChangedListener(this)
        }
    }

    /** Получить введённое значение как число */
    fun getValue(): Int {
        return try {
            current.replace("[^\\d]".toRegex(), "").toInt()
        } catch (e: Exception) {
            0
        }
    }
}