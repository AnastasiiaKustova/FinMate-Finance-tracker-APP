package com.example.financeapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.dbManager.OperationTypes
import com.example.financeapp.data.repository.OperationRepository
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.Constance.LOG_ERROR_OPERATION_ADD
import com.example.financeapp.utils.Constance.LOG_ERROR_OPERATION_LOAD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OperationViewModel(
    private val operationRepository: OperationRepository
) : ViewModel() {

    private val logTag = "OperationViewModel"

    private val _operations = MutableLiveData<List<OperationClassWithDate>>()
    val operations: LiveData<List<OperationClassWithDate>> = _operations

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> get() = _balance

    fun loadBalance() {
        viewModelScope.launch {
            val newBalance = operationRepository.getBalance()
            _balance.postValue(newBalance)
        }
    }

    fun loadDataWithQuery(query: Query, type: OperationTypes) {
        _operations.value = emptyList()
        operationRepository.setQuery(query)
        loadData(type)
    }

    fun loadData(type: OperationTypes) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = operationRepository.getTransactionsByDate(type)

                withContext(Dispatchers.Main) {
                    _operations.postValue(list)
                }


            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_OPERATION_LOAD, e)
                _errorMessage.postValue(LOG_ERROR_OPERATION_LOAD)
            }
        }
    }

    fun getBalance(query: Query, withSum: Boolean = true): Int {
        return operationRepository.getBalanceInt(query, withSum)
    }

    fun getTransactions(query: Query?): List<OperationClass>{
        return operationRepository.getTransactions(query)
    }

    fun addTransaction(transaction: OperationClass) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                operationRepository.createOperation(transaction)
            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_OPERATION_ADD, e)
                _errorMessage.postValue(LOG_ERROR_OPERATION_ADD)
            }
            loadData(OperationTypes.DATE)
        }
    }

    fun deleteFromDb(transaction: OperationClass) {
        viewModelScope.launch(Dispatchers.IO) {
            operationRepository.deleteFromDb(transaction.id)
            loadData(OperationTypes.DATE)
        }
    }

    fun updateOperation(transaction: OperationClass) {
        viewModelScope.launch(Dispatchers.IO) {
            operationRepository.updateOperation(transaction)
            loadData(OperationTypes.DATE)
        }
    }

    fun findOperationByCategory(categoryId: String): List<OperationClass> {
        return operationRepository.findTransactionsByCategory(categoryId)
    }

    override fun onCleared() {
        super.onCleared()
        operationRepository.close()
    }
}