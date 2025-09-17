package com.example.financeapp.data.repository

import com.example.financeapp.data.dbManager.DbManagerCards
import com.example.financeapp.data.model.CardsClass

class CardsRepository(private val dbManagerCards: DbManagerCards): AbstractRepository(dbManagerCards) {

    fun getCards(): ArrayList<CardsClass> {
            return dbManagerCards.readDbData(query_)
    }

    fun createCards(
        card: CardsClass,
    ): Pair<Long?,String>{
        val values = dbManagerCards.createValues(card)
        return dbManagerCards.insertToDb(values)
    }

    fun changeVisibility(card: CardsClass, visible: Int){
        dbManagerCards.changeVisibility(card.id, visible)
    }

    fun updateCard(
        card: CardsClass
    ){
        val values = dbManagerCards.createValues(card)
        dbManagerCards.updateInDb(card.id, values)
    }

}