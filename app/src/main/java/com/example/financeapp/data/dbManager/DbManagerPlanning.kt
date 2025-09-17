package com.example.financeapp.data.dbManager

import android.content.ContentValues
import android.content.Context
import com.example.financeapp.data.dbManager.dbHelper.DbHelperPlanning
import com.example.financeapp.data.model.PlanningClass
import com.example.financeapp.useCase.settings.Query

class DbManagerPlanning(override val context: Context) : DbManager(context) {

    override val _idColumn = DbTablePlanning.COLUMN_NAME_ID
    override val _UUIDColumn = DbTablePlanning.COLUMN_NAME_UUID
    private val _categoryUUIDColumn = DbTablePlanning.COLUMN_NAME_CATEGORY_UUID
    private val _moneyColumn = DbTablePlanning.COLUMN_NAME_MONEY
    private val _dateStartColumn = DbTablePlanning.COLUMN_NAME_DATE_START
    private val _dateEndColumn = DbTablePlanning.COLUMN_NAME_DATE_END
    private val _commentColumn = DbTablePlanning.COLUMN_NAME_COMMENT

    override val dbHelper = DbHelperPlanning(context)

    override fun tableName(): String {
        return DbTablePlanning.TABLE_NAME
    }

    fun createValues(
        planning: PlanningClass
    ): ContentValues {
        val values = ContentValues().apply {
            put(_categoryUUIDColumn, planning.categoryUUID)
            put(_moneyColumn, planning.planningValue)
            put(_dateStartColumn, planning.dateStart)
            put(_dateEndColumn, planning.dateEnd)
            put(_commentColumn, planning.comment)
        }
        return values
    }

    fun readDbData(
        query: Query
    ): ArrayList<PlanningClass> {
        val dataList = ArrayList<PlanningClass>()

        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                val itemID = getInt(getColumnIndexOrThrow(_idColumn))
                val itemUUID = getString(getColumnIndexOrThrow(_UUIDColumn))
                val itemCategoryUUID = getString(getColumnIndexOrThrow(_categoryUUIDColumn))
                val itemMoney = getInt(getColumnIndexOrThrow(_moneyColumn))
                val itemDateStart = getString(getColumnIndexOrThrow(_dateStartColumn)).toString()
                val itemDateEnd = getString(getColumnIndexOrThrow(_dateEndColumn)).toString()
                val itemComment = getString(getColumnIndexOrThrow(_commentColumn)).toString()

                val categoryClass = PlanningClass(
                    id = itemID,
                    UUID = itemUUID,
                    categoryUUID = itemCategoryUUID,
                    planningValue = itemMoney,
                    dateStart = itemDateStart,
                    dateEnd = itemDateEnd,
                    comment = itemComment
                )
                dataList.add(categoryClass)
            }
        }

        cursor?.close()

        return dataList
    }
}