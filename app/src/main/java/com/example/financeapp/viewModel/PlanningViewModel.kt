package com.example.financeapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.repository.OperationRepository
import com.example.financeapp.data.repository.PlanningRepository
import com.example.financeapp.data.model.PlanningClass
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance.LOG_ERROR_PLANNING_ADD
import com.example.financeapp.utils.Constance.LOG_ERROR_PLANNING_LOAD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanningViewModel(
    private val planningRepository: PlanningRepository,
    private val operationRepository: OperationRepository
) : ViewModel() {

    private val logTag = "PlanningViewModel"

    private val _planning = MutableLiveData<List<PlanningClass>>()
    val planning: LiveData<List<PlanningClass>> = _planning

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private fun setPlannings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = planningRepository.getPlannings()

                withContext(Dispatchers.Main) {
                    _planning.postValue(list)
                }
            } catch (e: Exception) {
                _planning.postValue(emptyList())
                _errorMessage.postValue(LOG_ERROR_PLANNING_LOAD)
            }
        }
    }

    fun loadData(query: Query) {
        planningRepository.setQuery(query)
        setPlannings()
    }

    fun loadPlanningWithSpent(
        plannings: List<PlanningClass>,
        onResult: (List<PlanningClass>) -> Unit
    ) {
        viewModelScope.launch {
            val updatedPlans = plannings.map { planning ->
                val query = Query(
                    selection = "${DbTableOperation.COLUMN_NAME_CATEGORY_UUID} = ? AND ${DbTableOperation.COLUMN_NAME_DATE} BETWEEN ? AND ?",
                    selectionArgs = arrayOf(planning.categoryUUID, planning.dateStart, planning.dateEnd),
                    sortOrder = null
                )
                val balance = operationRepository.getBalanceInt(query, false)
                planning.copy(spentValue = balance)
            }
            onResult(updatedPlans)
        }
    }

    fun addPlanning(planning: PlanningClass) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
            planningRepository.createPlannings(planning)
            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_PLANNING_ADD, e)
                _errorMessage.postValue(LOG_ERROR_PLANNING_ADD)
            }
            setPlannings()
        }
    }

    fun updatePlanning(planning: PlanningClass) {
        viewModelScope.launch(Dispatchers.IO) {
            planningRepository.updatePlanning(planning)
            setPlannings()
        }
    }

    fun deletePlanning(planning: PlanningClass) {
        viewModelScope.launch(Dispatchers.IO) {
            planningRepository.deleteFromDb(planning.id)
            setPlannings()
        }
    }

    override fun onCleared() {
        super.onCleared()
        planningRepository.close()
        operationRepository.close()
    }
}