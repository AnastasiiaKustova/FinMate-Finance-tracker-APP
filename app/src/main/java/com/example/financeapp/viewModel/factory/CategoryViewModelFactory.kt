package com.example.financeapp.viewModel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.data.dbManager.DbManagerCategory
import com.example.financeapp.data.repository.CategoryRepository
import com.example.financeapp.viewModel.CategoryViewModel

class CategoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            val dbManager = DbManagerCategory(context.applicationContext)
            val repository = CategoryRepository(dbManager)
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}