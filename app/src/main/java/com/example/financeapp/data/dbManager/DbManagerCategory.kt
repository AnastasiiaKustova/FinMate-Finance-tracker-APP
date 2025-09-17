package com.example.financeapp.data.dbManager

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.financeapp.data.dbManager.dbHelper.DbHelperCategory
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.useCase.settings.Query

class DbManagerCategory(override val context: Context) : DbManager(context) {

    override val _idColumn = DbTableCategory.COLUMN_NAME_ID
    override val _UUIDColumn = DbTableCategory.COLUMN_NAME_UUID
    private val _titleColumn = DbTableCategory.COLUMN_NAME_TITLE
    private val _typeColumn = DbTableCategory.COLUMN_NAME_TYPE
    private val _iconIdColumn = DbTableCategory.COLUMN_NAME_ICON_ID
    private val _colorIdColumn = DbTableCategory.COLUMN_NAME_COLOR_ID

    override val dbHelper = DbHelperCategory(context)

    override fun tableName(): String {
        return DbTableCategory.TABLE_NAME
    }

    fun createValues(
        category: CategoryClass
    ): ContentValues {
        val values = ContentValues().apply {
            put(_titleColumn, category.title)
            put(_typeColumn, category.typeOperation)
            put(_iconIdColumn, category.iconId)
            put(_colorIdColumn, category.colorId)
        }
        return values
    }

    fun readDbData(query: Query, size: Int = 0): ArrayList<CategoryClass> {
        val dataList = ArrayList<CategoryClass>()

        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                if (size != 0 && dataList.size >= size)
                    break

                val categoryClass = fillCategoryClass(this)
                dataList.add(categoryClass)
            }
        }

        cursor?.close()

        return dataList
    }

    fun getSizeBySelectedType(selectedType: String): Int {
        val selection = "(${_typeColumn} = ?)"
        val selectionArgs = arrayOf(selectedType)

        return getSize(Query(selection, selectionArgs, null))
    }

    private fun fillCategoryClass(cursor: Cursor): CategoryClass {
        with(cursor) {
            val itemID = getInt(getColumnIndexOrThrow(_idColumn))
            val itemUUID = getString(getColumnIndexOrThrow(_UUIDColumn))
            val itemTitle = getString(getColumnIndexOrThrow(_titleColumn)).toString()
            val itemType = getString(getColumnIndexOrThrow(_typeColumn)).toString()
            val itemIcon = getInt(getColumnIndexOrThrow(_iconIdColumn))
            val itemColor = getString(getColumnIndexOrThrow(_colorIdColumn)).toString()

            return CategoryClass(
                id = itemID,
                UUID = itemUUID,
                title = itemTitle,
                typeOperation = itemType,
                iconId = itemIcon,
                colorId = itemColor
            )
        }
    }

    fun findByQuery(query: Query): CategoryClass? {

        var data: CategoryClass? = null

        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                data = fillCategoryClass(this)
                break
            }
        }

        cursor?.close()

        return data
    }
}