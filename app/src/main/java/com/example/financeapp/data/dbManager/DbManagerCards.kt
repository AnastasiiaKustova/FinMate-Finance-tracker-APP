package com.example.financeapp.data.dbManager

import android.content.ContentValues
import android.content.Context
import com.example.financeapp.data.dbManager.dbHelper.DbHelperCards
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.useCase.settings.Query

class DbManagerCards(context: Context): DbManager(context) {

    override val _idColumn = DbTableCards.COLUMN_NAME_ID
    override val _UUIDColumn = DbTableCards.COLUMN_NAME_UUID
    private val _titleColumn = DbTableCards.COLUMN_NAME_TITLE
    private val _dateColumn = DbTableCards.COLUMN_NAME_DATE
    private val _visibleColumn = DbTableCards.COLUMN_NAME_VISIBLE
    private val _commentColumn = DbTableCards.COLUMN_NAME_COMMENT

    override val dbHelper = DbHelperCards(context)

    override fun tableName(): String {
        return DbTableCards.TABLE_NAME
    }

    fun createValues(
        card: CardsClass
    ): ContentValues {
        val values = ContentValues().apply {
            put(_titleColumn, card.title)
            put(_dateColumn, card.date)
            put(_commentColumn, card.comment)
            put(_visibleColumn, card.visible)
        }
        return values
    }

    fun readDbData(query: Query): ArrayList<CardsClass> {
        val dataList = ArrayList<CardsClass>()
        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                val itemID = getInt(getColumnIndexOrThrow(_idColumn))
                val itemUUID = getString(getColumnIndexOrThrow(_UUIDColumn))
                val itemTitle = getString(getColumnIndexOrThrow(_titleColumn)).toString()
                val itemDate = getString(getColumnIndexOrThrow(_dateColumn)).toString()
                val itemVisible = getInt(getColumnIndexOrThrow(_visibleColumn))
                val itemComment = getString(getColumnIndexOrThrow(_commentColumn)).toString()

                val cardsClass = CardsClass(
                    id = itemID,
                    UUID = itemUUID,
                    title = itemTitle,
                    date = itemDate,
                    visible = itemVisible,
                    comment = itemComment
                )
                dataList.add(cardsClass)
            }
        }

        cursor?.close()

        return dataList
    }

    fun changeVisibility(id : Int, visible: Int){
        val values = ContentValues().apply {
            put(_visibleColumn, visible)
        }
        updateInDb(id, values)
    }
}