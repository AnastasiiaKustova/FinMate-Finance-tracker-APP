package com.example.financeapp.data.dbManager

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.financeapp.data.dbManager.dbHelper.DbHelperOperation
import com.example.financeapp.data.model.AdditionalInfo
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.Months

class DbManagerOperation(override val context: Context) : DbManager(context) {

    override val _idColumn = DbTableOperation.COLUMN_NAME_ID
    override val _UUIDColumn = DbTableOperation.COLUMN_NAME_UUID
    private val _typeColumn = DbTableOperation.COLUMN_NAME_TYPE
    private val _categoryUUIDColumn = DbTableOperation.COLUMN_NAME_CATEGORY_UUID
    private val _dateColumn = DbTableOperation.COLUMN_NAME_DATE
    private val _commentColumn = DbTableOperation.COLUMN_NAME_COMMENT
    private val _cardUUIDColumn = DbTableOperation.COLUMN_NAME_CARD_UUID
    private val _moneyColumn = DbTableOperation.COLUMN_NAME_MONEY

    override val dbHelper = DbHelperOperation(context)

    override fun tableName(): String {
        return DbTableOperation.TABLE_NAME
    }

    fun createValues(
        transaction: OperationClass
    ): ContentValues {
        val values = ContentValues().apply {
            put(_typeColumn, transaction.type)
            put(_categoryUUIDColumn, transaction.categoryUUID)
            put(_dateColumn, transaction.date)
            put(_commentColumn, transaction.comment)
            put(_cardUUIDColumn, transaction.cardUUID)
            put(_moneyColumn, transaction.money)
        }
        return values
    }

    private fun fillOperationClass(cursor: Cursor): OperationClass {
        with(cursor) {
            val itemID = getInt(getColumnIndexOrThrow(_idColumn))
            val itemUUID = getString(getColumnIndexOrThrow(_UUIDColumn))
            val itemType = getString(getColumnIndexOrThrow(_typeColumn)).toString()
            val itemCategoryUUID = getString(getColumnIndexOrThrow(_categoryUUIDColumn))
            val itemDate = getString(getColumnIndexOrThrow(_dateColumn)).toString()
            val itemComment = getString(getColumnIndexOrThrow(_commentColumn)).toString()
            val itemCardUUID = getString(getColumnIndexOrThrow(_cardUUIDColumn))
            val itemMoney = getInt(getColumnIndexOrThrow(_moneyColumn))

            val operationClass = OperationClass(
                categoryUUID = itemCategoryUUID,
                id = itemID,
                UUID = itemUUID,
                type = itemType,
                date = itemDate,
                comment = itemComment,
                cardUUID = itemCardUUID,
                money = itemMoney
            )

            return operationClass
        }
    }

    fun readDbData(
        query: Query,
    ): ArrayList<OperationClass> {
        val dataList = ArrayList<OperationClass>()
        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                val operationClass = fillOperationClass(this)
                dataList.add(operationClass)
            }
        }
        cursor?.close()

        return dataList
    }

    fun getBalance(
        query: Query,
        withSum: Boolean = false,
    ): Int {
        var balance = 0

        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                val itemMoney = getInt(getColumnIndexOrThrow(_moneyColumn))
                val itemTitle = getString(getColumnIndexOrThrow(_typeColumn)).toString()

                if ((itemTitle == Constance.expense || itemTitle == Constance.minus) && withSum)
                    balance -= itemMoney
                else
                    balance += itemMoney
            }
        }

        cursor?.close()

        return balance
    }

    fun readDbDataByDate(
        query: Query,
        type: OperationTypes
    ): ArrayList<OperationClassWithDate> {
        var dataList = ArrayList<OperationClass>()
        var dataListWithDate = ArrayList<OperationClassWithDate>()

        val cursor = getCursor(query)

        with(cursor) {
            var currentDate = ""
            var money = 0
            var currentCategoryUUID = ""
            while (this?.moveToNext()!!) {

                val operationClass = fillOperationClass(this)

                val comparisonDate = when (type){
                    OperationTypes.ADMISSION -> Months.getNameOfMonth(operationClass.date)
                    else -> operationClass.date
                }

                if (currentCategoryUUID == "") {
                    currentCategoryUUID = operationClass.categoryUUID

                }

                if (currentDate.isEmpty()) currentDate = comparisonDate

                val isNextList = when (type){
                    OperationTypes.CATEGORY -> (currentCategoryUUID != operationClass.categoryUUID)
                    else -> (currentDate != comparisonDate)
                }

                if (isNextList) {

                    val operationClassWithDate = OperationClassWithDate(
                        date = currentDate,
                        dataList = dataList,
                        info = AdditionalInfo(
                            money = money,
                            categoryUUID = currentCategoryUUID,
                        )
                    )
                    dataListWithDate.add(operationClassWithDate)
                    dataList = ArrayList()
                    currentDate = comparisonDate
                    money = 0
                    currentCategoryUUID = operationClass.categoryUUID
                }
                money += operationClass.money
                dataList.add(operationClass)
            }

            val isHaveData = when (type){
                OperationTypes.ADMISSION -> money > 0
                else -> currentDate != ""
            }

            if (isHaveData ) {

                val operationClassWithDate = OperationClassWithDate(
                    date = currentDate,
                    dataList = dataList,
                    info = AdditionalInfo(
                        money = money,
                        categoryUUID = currentCategoryUUID,
                    )
                )
                dataListWithDate.add(operationClassWithDate)
                dataList = ArrayList()
            }
        }
        cursor?.close()

        return dataListWithDate
    }
}