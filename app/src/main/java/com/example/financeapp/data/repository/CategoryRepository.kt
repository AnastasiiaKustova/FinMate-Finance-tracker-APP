package com.example.financeapp.data.repository

import com.example.financeapp.data.dbManager.DbManagerCategory
import com.example.financeapp.data.dbManager.DbTableCategory
import com.example.financeapp.data.model.CategoryClass

class CategoryRepository(private val dbManagerCategory: DbManagerCategory): AbstractRepository(dbManagerCategory) {

    fun getCategories(size: Int? = null): ArrayList<CategoryClass>{
        if (size == null)
            return dbManagerCategory.readDbData(query_)
        else
            return ArrayList(dbManagerCategory.readDbData(query_).take(size))
    }

    fun createCategories(
        category: CategoryClass
    ): Pair<Long?,String>{
        val values = dbManagerCategory.createValues(category)
        return dbManagerCategory.insertToDb(values)
    }

    fun updateCategories(
        category: CategoryClass
    ){
        val values = dbManagerCategory.createValues(category)
        dbManagerCategory.updateInDb(category.id, values)
    }

    fun findByTitleAndType(title: String, type: String): CategoryClass?{
        setSettings(
            selection = "${DbTableCategory.COLUMN_NAME_TITLE} == ? AND ${DbTableCategory.COLUMN_NAME_TYPE} == ?",
            selectionArgs = arrayOf(title, type),
            sortOrder = null
        )
        return dbManagerCategory.findByQuery(query_)
    }

    fun findByUUID(UUID: String): CategoryClass?{
        setSettings(
            selection = "${DbTableCategory.COLUMN_NAME_UUID} == ?",
            selectionArgs = arrayOf(UUID),
            sortOrder = null
        )
        return dbManagerCategory.findByQuery(query_)
    }
}