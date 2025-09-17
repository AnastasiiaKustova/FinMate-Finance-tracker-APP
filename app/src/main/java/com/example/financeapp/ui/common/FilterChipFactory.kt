package com.example.financeapp.ui.common

import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.financeapp.R
import com.example.financeapp.data.model.FilterChip
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.StringNames
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

object FilterChipFactory {

    fun addChips(
        chipGroup: ChipGroup,
        filters: List<FilterChip>,
        onRemove: ((FilterChip) -> Unit)? = null,
        colorRes: Int? = null
    ) {
        chipGroup.removeAllViews()

        for (filter in filters) {
            val chip = Chip(chipGroup.context).apply {
                text = filter.title
                isCloseIconVisible = true
                isCheckable = false

                colorRes?.let {
                    chipBackgroundColor = ContextCompat.getColorStateList(context, it)
                }

                setOnCloseIconClickListener {
                    chipGroup.removeView(this)
                    onRemove?.invoke(filter)
                }
            }

            chip.setChipBackgroundColorResource(R.color.Grey)
            chipGroup.addView(chip)
        }
    }

    fun operationTypesToFilterChip(operationTypes: List<String>?, stringNames: StringNames): List<FilterChip> {
        if (operationTypes.isNullOrEmpty())
            return emptyList()

        val filterChips = mutableListOf<FilterChip>()

        for (o in operationTypes) {
            filterChips.add(
                FilterChip(
                    tag = o,
                    title = when (o) {
                        Constance.admission -> stringNames.ADMISSION_OP
                        Constance.expense -> stringNames.EXPENSE_OP
                        Constance.minus -> stringNames.TRANSFERS
                        else -> o
                    }
                )
            )
        }

        return filterChips
    }
}