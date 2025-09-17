package com.example.financeapp.data.dbManager

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.financeapp.data.dbManager.dbHelper.DbHelper
import com.example.financeapp.useCase.settings.Query
import java.util.UUID

abstract class DbManager(open val context: Context) {

    private var db: SQLiteDatabase? = null
    abstract val dbHelper: DbHelper
    abstract fun tableName(): String
    abstract val _idColumn: String
    abstract val _UUIDColumn: String

    fun openDb() {
        db = dbHelper.writableDatabase
    }

    fun readDb() {
        db = dbHelper.readableDatabase
    }

    fun closeDb() {
        dbHelper.close()
    }

    fun insertToDb(values: ContentValues): Pair<Long?, String> {

        val newUUID = UUID.randomUUID().toString()
        values.put(_UUIDColumn, newUUID)
        val newId: Long? = db?.insert(tableName(), null, values)

        return Pair(newId, newUUID)
    }

    fun getCursor(
        query: Query,
    ): Cursor? {
        readDb()
        val projection = null
        val cursor = db?.query(
            tableName(),   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            query.selection,              // The columns for the WHERE clause
            query.selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            query.sortOrder               // The sort order
        )
        return cursor
    }

    fun getSize(
        query: Query,
    ): Int {

        var size = 0

        val cursor = getCursor(query)

        with(cursor) {
            while (this?.moveToNext()!!) {
                size++
            }
        }
        cursor?.close()

        return size

    }

    //Обновление базы
    fun updateInDb(id: Int, values: ContentValues) {

        val selection = "${_idColumn} LIKE ?"
        val selectionArgs = arrayOf(id.toString())

        db?.update(tableName(), values, selection, selectionArgs)
    }

    //Удаление
    fun deleteFromDb(id: Int): Int {
        var deletedRows = 0
        val selection = "${_idColumn} LIKE ?"
        val selectionArgs = arrayOf(id.toString())
        deletedRows = db?.delete(tableName(), selection, selectionArgs)!!

        return deletedRows
    }

    fun deleteDb(
        db: SQLiteDatabase,
        columnName: String,
        columnValue: String,
        tableName: String
    ): Int {
        val selection = "$columnName LIKE ?"
        val selectionArgs = arrayOf(columnValue)
        val deletedRows = db.delete(tableName, selection, selectionArgs)

        return deletedRows
    }

}
