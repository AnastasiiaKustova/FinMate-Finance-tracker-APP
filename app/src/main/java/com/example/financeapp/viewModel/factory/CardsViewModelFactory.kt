package com.example.financeapp.viewModel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.data.dbManager.DbManagerCards
import com.example.financeapp.data.dbManager.DbManagerOperation
import com.example.financeapp.data.repository.CardsRepository
import com.example.financeapp.data.repository.OperationRepository
import com.example.financeapp.viewModel.CardsViewModel

class CardsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardsViewModel::class.java)) {
            val dbManager = DbManagerCards(context.applicationContext)
            val repository = CardsRepository(dbManager)
            val dbManagerOperation = DbManagerOperation(context.applicationContext)
            val operationRepo = OperationRepository(dbManagerOperation)
            @Suppress("UNCHECKED_CAST")
            return CardsViewModel(repository, operationRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}