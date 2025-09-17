package com.example.financeapp.data.dbManager

import android.provider.BaseColumns

object DbTableOperation : BaseColumns {
    const val TABLE_NAME = "TransactionDB"
    const val COLUMN_NAME_TYPE = "Type"
    const val COLUMN_NAME_CATEGORY_UUID = "CategoryUUID"
    const val COLUMN_NAME_DATE = "Date"
    const val COLUMN_NAME_COMMENT = "Comment"
    const val COLUMN_NAME_CARD_UUID = "CardUUID"
    const val COLUMN_NAME_MONEY = "Money"
    const val COLUMN_NAME_ID = "_id"
    const val COLUMN_NAME_UUID = "UUID"

    const val DATABASE_NAME = "FinanceApp.db"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "$COLUMN_NAME_UUID TEXT," +
            "$COLUMN_NAME_TYPE TEXT," +
            "$COLUMN_NAME_CATEGORY_UUID TEXT," +
            "$COLUMN_NAME_DATE DATE," +
            "$COLUMN_NAME_COMMENT TEXT," +
            "$COLUMN_NAME_CARD_UUID TEXT," +
            "$COLUMN_NAME_MONEY INTEGER)"

    const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}

object DbTableCategory : BaseColumns {
    const val TABLE_NAME = "CategoryTable"
    const val COLUMN_NAME_TITLE = "Title"
    const val COLUMN_NAME_ICON_ID = "IconID"
    const val COLUMN_NAME_COLOR_ID = "ColorID"
    const val COLUMN_NAME_TYPE = "TypeOperation"
    const val COLUMN_NAME_ID = "_id"
    const val COLUMN_NAME_UUID = "UUID"

    const val DATABASE_NAME = "FinanceAppCategory.db"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "$COLUMN_NAME_UUID TEXT," +
            "$COLUMN_NAME_TITLE TEXT," +
            "$COLUMN_NAME_TYPE TEXT," +
            "$COLUMN_NAME_ICON_ID INT," +
            "$COLUMN_NAME_COLOR_ID TEXT)"

    const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}

object DbTablePlanning : BaseColumns {
    const val TABLE_NAME = "PlanningTable"
    const val COLUMN_NAME_CATEGORY_UUID = "CategoryUUID"
    const val COLUMN_NAME_MONEY = "Money"
    const val COLUMN_NAME_DATE_START = "DateStart"
    const val COLUMN_NAME_DATE_END = "DateEnd"
    const val COLUMN_NAME_COMMENT = "Comment"
    const val COLUMN_NAME_ID = "_id"
    const val COLUMN_NAME_UUID = "UUID"

    const val DATABASE_NAME = "FinanceAppPlanning.db"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "$COLUMN_NAME_ID INTEGER PRIMARY KEY," +
            "$COLUMN_NAME_UUID TEXT," +
            "$COLUMN_NAME_CATEGORY_UUID TEXT," +
            "$COLUMN_NAME_MONEY INTEGER," +
            "$COLUMN_NAME_DATE_START DATE," +
            "$COLUMN_NAME_DATE_END DATE," +
            "$COLUMN_NAME_COMMENT TEXT)"

    const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}

object DbTableCards : BaseColumns {
    const val TABLE_NAME = "CardsTable"
    const val COLUMN_NAME_TITLE = "Title"
    const val COLUMN_NAME_DATE = "Date"
    const val COLUMN_NAME_COMMENT = "Comment"
    const val COLUMN_NAME_VISIBLE = "Visible"
    const val COLUMN_NAME_ID = "_id"
    const val COLUMN_NAME_UUID = "UUID"

    const val DATABASE_NAME = "FinanceAppCards.db"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "$COLUMN_NAME_ID INTEGER PRIMARY KEY," +
            "$COLUMN_NAME_UUID TEXT," +
            "$COLUMN_NAME_TITLE TEXT," +
            "$COLUMN_NAME_DATE DATE," +
            "$COLUMN_NAME_VISIBLE INTEGER," +
            "$COLUMN_NAME_COMMENT TEXT)"

    const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}
