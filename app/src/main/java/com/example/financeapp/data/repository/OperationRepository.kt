package com.example.financeapp.data.repository

import com.example.financeapp.data.dbManager.DbManagerOperation
import com.example.financeapp.data.dbManager.OperationTypes
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.MoneyFormatter

class OperationRepository(private val dbManagerOperation: DbManagerOperation): AbstractRepository(dbManagerOperation) {

    fun getTransactions(query: Query? = null): ArrayList<OperationClass>{
        return if (query == null)
            dbManagerOperation.readDbData(query_)
        else dbManagerOperation.readDbData(query)
    }

    fun getTransactionsByDate(type: OperationTypes): ArrayList<OperationClassWithDate>{
        return dbManagerOperation.readDbDataByDate(query_, type)
    }

    fun findTransactionsByCategory(categoryId: String): List<OperationClass> {
        return dbManagerOperation.readDbData(Query.operationByCategoryQuery(categoryId))
    }

    fun getBalance(query: Query? = null, withSum: Boolean = true):String{
        return MoneyFormatter.format(getBalanceInt(query, withSum))
    }

    fun getBalanceInt(query: Query? = null, withSum: Boolean = true):Int{
        return if (query == null)
            dbManagerOperation.getBalance(query_, withSum)
        else
            dbManagerOperation.getBalance(query, withSum)
    }

    fun createOperation(
        transaction: OperationClass
    ){
        val values = dbManagerOperation.createValues(transaction)

        dbManagerOperation.insertToDb(values)
    }

    fun updateOperation(
        transaction: OperationClass
    ){
        val values = dbManagerOperation.createValues(transaction)
        dbManagerOperation.updateInDb(transaction.id, values)
    }

}
