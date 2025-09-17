package com.example.financeapp.utils

import android.content.Context
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter

object CategoryUiMapper {
    private fun buildCategoryList(
        context: Context,
        categories: List<CategoryClass>,
        selectedCategory: CategoryClass?
    ): List<CategoryClass> {
        val result = ArrayList<CategoryClass>()

        if (categories.size > 4) {
            result.addAll(categories.take(3))
            result.add(CategoryIcons.getMoreIcon(context))
        } else {
            result.addAll(categories)
        }

        result.add(CategoryIcons.getAddIcon(context))

        if (selectedCategory != null) {
            var foundIndex = -1

            for ((i, c) in result.withIndex()) {
                if (c.id == selectedCategory.id)
                    foundIndex = i
            }

            if (foundIndex != -1)
                result.removeAt(foundIndex)
            else if (result.size > 3)
                result.removeAt(2)

            result.add(0, selectedCategory)
        }

        return result
    }

    fun updateCategories(
        context: Context,
        adapter: ItemsCategoryAdapter,
        selectedType: String,
        selectedCategory: CategoryClass?,
        newCategory: CategoryClass?,
        categories : List<CategoryClass>
    ):  CategoryClass?{

        var newSelectedCategory: CategoryClass? = selectedCategory

        for (c in categories){
            if (c.typeOperation != selectedType && c.typeOperation.isNotEmpty())
                return null
        }

        if (newCategory != null){
            newSelectedCategory = newCategory

        }

        if (selectedCategory?.typeOperation != selectedType)
            newSelectedCategory = null

        val categories_ = buildCategoryList(context, categories, newSelectedCategory)

        adapter.updateData(categories_)
        adapter.unselectAllPositions()

        if (categories_.isNotEmpty()) {
            if (categories_[0].id != 0)
                newSelectedCategory = categories_[0]
            adapter.setSelectedPosition(0)
        }

        return newSelectedCategory
    }
}