package com.example.financeapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.repository.CardsRepository
import com.example.financeapp.data.repository.OperationRepository
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance.LOG_ERROR_CARD_ADD
import com.example.financeapp.utils.Constance.LOG_ERROR_CARD_LOAD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CardsViewModel(
    private val cardsRepository: CardsRepository,
    private val operationRepository: OperationRepository
) : ViewModel() {

    private val logTag = "CardsViewModel"

    private val _cards = MutableLiveData<List<CardsClass>>()
    val cards: LiveData<List<CardsClass>> = _cards

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _cardCreatedUUID = MutableLiveData<String?>()
    val cardCreatedUUID: LiveData<String?> = _cardCreatedUUID

    fun addCard(card: CardsClass) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val createCard = cardsRepository.createCards(card)
                val newId = createCard.first
                val newUUID = createCard.second
                if (newId == -1L || newId == null) {
                    Log.e(logTag, LOG_ERROR_CARD_ADD)
                    _errorMessage.postValue(LOG_ERROR_CARD_ADD)
                } else {
                    _cardCreatedUUID.postValue(newUUID)
                }
            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_CARD_ADD, e)
                _errorMessage.postValue(LOG_ERROR_CARD_ADD)
            }
            loadData()
        }
    }

    fun getBalance(card: CardsClass): Int {
        try {
            val query = Query(
                selection = "${DbTableOperation.COLUMN_NAME_CARD_UUID} = ?",
                selectionArgs = arrayOf(card.UUID),
                sortOrder = null
            )
            return operationRepository.getBalanceInt(query)
        } catch (e: Exception) {
            return 0
        }
    }

    fun loadData(query: Query? = null) {

        cardsRepository.setQuery(query ?: Query.empty())

        viewModelScope.launch(Dispatchers.IO) {
            _cardCreatedUUID.postValue(null)
            try {
                val list = cardsRepository.getCards()

                for (item in list) {
                    item.balance = getBalance(item)
                }

                withContext(Dispatchers.Main) {
                    _cards.postValue(list)
                }
            } catch (e: Exception) {
                Log.e(logTag, LOG_ERROR_CARD_LOAD, e)
                _errorMessage.postValue(LOG_ERROR_CARD_LOAD)
            }
        }
    }

    fun toggleCardVisibility(card: CardsClass) {
        val visible = if (card.visible == 1) 0 else 1
        viewModelScope.launch {
            cardsRepository.changeVisibility(card, visible)
            loadData()
        }
    }

    fun deleteFromDb(card: CardsClass) {
        viewModelScope.launch {
            cardsRepository.deleteFromDb(card.id)
            loadData()
        }
    }

    fun updateCard(card: CardsClass) {
        viewModelScope.launch(Dispatchers.IO) {
            cardsRepository.updateCard(card)
            loadData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cardsRepository.close()
        operationRepository.close()
    }
}