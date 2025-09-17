package com.example.financeapp.data.repository

import com.example.financeapp.data.dbManager.DbManager
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.useCase.settings.SettingsQueryBuilder

abstract class AbstractRepository(var dbManager: DbManager) {

    var query_ = Query.empty()

    fun setSettings(selection: String?, selectionArgs: Array<String>?, sortOrder: String?){
        setQuery(Query(selection, selectionArgs, sortOrder))
    }

    fun setQuery(query: Query){
        query_ = query
    }

    fun deleteFromDb(id : Int){
        dbManager.deleteFromDb(id)
    }

    fun close(){
        dbManager.closeDb()
    }

}