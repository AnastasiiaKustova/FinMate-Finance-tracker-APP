package com.example.financeapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.data.repository.CategoryRepository
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance.LOG_ERROR_CARD_ADD
import com.example.financeapp.utils.Constance.LOG_ERROR_CATEGORY_ADD
import com.example.financeapp.utils.Constance.LOG_ERROR_CATEGORY_LOAD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val logTag = "CategoryViewModel"

    private val _categories = MutableLiveData<List<CategoryClass>>()
    val categories: LiveData<List<CategoryClass>> = _categories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _categoryCreatedUUID = MutableLiveData<String?>()
    val categoryCreatedUUID: LiveData<String?> = _categoryCreatedUUID

    fun getQuery():Query = categoryRepository.query_

    fun loadData(query: Query) {
        //_categories.value = emptyList()
        categoryRepository.setQuery(query)
        setCategories()
    }

    fun getAllCategories() {
        //_categories.value = emptyList()
        categoryRepository.setQuery(Query.defaultForAllCategory())
        setCategories()
    }

    private fun setCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            _categoryCreatedUUID.postValue(null)
            try {
                val list = categoryRepository.getCategories()
                withContext(Dispatchers.Main) {
                    _categories.postValue(list)
                }
            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_CATEGORY_LOAD, e)
                _errorMessage.postValue(LOG_ERROR_CATEGORY_LOAD)
            }
        }
    }

    fun addCategory(category: CategoryClass) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val createCategories = categoryRepository.createCategories(category)
                val newId = createCategories.first
                val newUUID = createCategories.second
                if (newId == -1L || newId == null) {
                    Log.e(logTag, LOG_ERROR_CATEGORY_ADD)
                    _errorMessage.postValue(LOG_ERROR_CATEGORY_ADD)
                } else {
                    _categoryCreatedUUID.postValue(newUUID)
                }
            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_CATEGORY_ADD, e)
                _errorMessage.postValue(LOG_ERROR_CATEGORY_ADD)
            }
            setCategories()
        }
    }

    fun findCategoryByTitleAndType(title: String, type: String): CategoryClass? {
        return categoryRepository.findByTitleAndType(title, type)
    }

    fun findCategoryByUUID(UUID: String): CategoryClass? {
        return categoryRepository.findByUUID(UUID)
    }

    fun updateCategory(category: CategoryClass) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.updateCategories(category)
            setCategories()
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.deleteFromDb(id)
            setCategories()
        }
    }

    override fun onCleared() {
        super.onCleared()
        categoryRepository.close()
    }
}