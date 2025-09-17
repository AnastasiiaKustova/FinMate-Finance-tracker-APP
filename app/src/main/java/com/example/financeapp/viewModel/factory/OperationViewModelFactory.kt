package com.example.financeapp.viewModel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.data.dbManager.DbManagerOperation
import com.example.financeapp.data.repository.OperationRepository
import com.example.financeapp.viewModel.OperationViewModel

class OperationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OperationViewModel::class.java)) {
            val dbManager = DbManagerOperation(context.applicationContext)
            val repository = OperationRepository(dbManager)
            @Suppress("UNCHECKED_CAST")
            return OperationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}