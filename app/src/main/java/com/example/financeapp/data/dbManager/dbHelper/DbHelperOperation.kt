package com.example.financeapp.data.dbManager.dbHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.financeapp.data.dbManager.DbTableCards
import com.example.financeapp.data.dbManager.DbTableCategory
import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.dbManager.DbTablePlanning

abstract class DbHelper(context: Context, DATABASE_NAME: String, DATABASE_VERSION: Int) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    abstract fun tableNameCreate(): String
    abstract fun tableNameDelete(): String

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(tableNameCreate())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(tableNameDelete())
        onCreate(db)
    }
}

class DbHelperOperation(context: Context) : DbHelper(context, DATABASE_NAME, DATABASE_VERSION) {

    override fun tableNameCreate(): String {
        return DbTableOperation.CREATE_TABLE
    }

    override fun tableNameDelete(): String {
        return DbTableOperation.DELETE_TABLE
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = DbTableOperation.DATABASE_NAME
    }

}

class DbHelperCategory(context: Context) : DbHelper(context, DATABASE_NAME, DATABASE_VERSION) {

    override fun tableNameCreate(): String {
        return DbTableCategory.CREATE_TABLE
    }

    override fun tableNameDelete(): String {
        return DbTableCategory.DELETE_TABLE
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = DbTableCategory.DATABASE_NAME
    }
}

class DbHelperPlanning(context: Context) : DbHelper(context, DATABASE_NAME, DATABASE_VERSION) {

    override fun tableNameCreate(): String {
        return DbTablePlanning.CREATE_TABLE
    }

    override fun tableNameDelete(): String {
        return DbTablePlanning.DELETE_TABLE
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = DbTablePlanning.DATABASE_NAME
    }
}

class DbHelperCards(context: Context) : DbHelper(context, DATABASE_NAME, DATABASE_VERSION) {

    override fun tableNameCreate(): String {
        return DbTableCards.CREATE_TABLE
    }

    override fun tableNameDelete(): String {
        return DbTableCards.DELETE_TABLE
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = DbTableCards.DATABASE_NAME
    }
}

