package com.example.financeapp.viewModel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.data.dbManager.DbManagerOperation
import com.example.financeapp.data.dbManager.DbManagerPlanning
import com.example.financeapp.data.repository.OperationRepository
import com.example.financeapp.data.repository.PlanningRepository
import com.example.financeapp.viewModel.PlanningViewModel

class PlanningViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlanningViewModel::class.java)) {
            val dbManagerPlanning = DbManagerPlanning(context.applicationContext)
            val planningRepo = PlanningRepository(dbManagerPlanning)
            val dbManagerOperation = DbManagerOperation(context.applicationContext)
            val operationRepo = OperationRepository(dbManagerOperation)
            @Suppress("UNCHECKED_CAST")
            return PlanningViewModel(planningRepo, operationRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}