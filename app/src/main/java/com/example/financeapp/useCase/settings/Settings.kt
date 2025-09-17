package com.example.financeapp.useCase.settings


import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.model.SettingsItemClass
import com.example.financeapp.utils.Constance.plus
import com.example.financeapp.utils.DateUtils

data class Settings(
    val dateStart: String,
    val dateEnd: String,
    val operationTypes: List<String>? = null,
    val operationTypesExclude: List<String>? = null,
    val categoryIDs: List<SettingsItemClass>? = null,
    val orderBy: String? = null
){
    companion object {
        fun default(): Settings {
            val dateStartText = DateUtils.getCurrentDateStart()
            val dateEndText = DateUtils.getCurrentDate()

            return Settings(
                dateStart = dateStartText,
                dateEnd = dateEndText,
                operationTypesExclude = arrayListOf(plus),
                orderBy = "${DbTableOperation.COLUMN_NAME_DATE} DESC, ${DbTableOperation.COLUMN_NAME_ID} DESC"
            )
        }
    }
}