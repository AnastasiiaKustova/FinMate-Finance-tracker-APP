package com.example.financeapp.useCase.settings

import android.content.Context
import com.example.financeapp.data.dbManager.DbTableCards
import com.example.financeapp.data.dbManager.DbTableCategory
import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.dbManager.DbTablePlanning
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.utils.CommentUtils
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.utils.StringNames

data class Query(
    val selection: String?,
    val selectionArgs: Array<String>?,
    val sortOrder: String?
) {
    companion object {
        fun empty(): Query {
            return Query(
                null,
                null,
                null
            )
        }

        fun defaultForPlanning(dStart: String? = null, dEnd: String? = null): Query {
            val dateStart = dStart ?: DateUtils.getCurrentDateStart()
            val dateEnd = dEnd ?: DateUtils.getPlanningDateEnd()

            return Query(
                selection = "${DbTablePlanning.COLUMN_NAME_DATE_END} <= ? AND ${DbTablePlanning.COLUMN_NAME_DATE_START} >= ?",
                selectionArgs = arrayOf(dateEnd, dateStart),
                sortOrder = null
            )
        }

        fun defaultForCategory(categoryType: String): Query {
            return Query(
                selection = "(${DbTableCategory.COLUMN_NAME_TYPE} = ?)",
                selectionArgs = arrayOf(categoryType),
                sortOrder = "${DbTableCategory.COLUMN_NAME_TITLE} ASC"
            )
        }

        fun defaultForAllCategory(): Query {
            return Query(
                selection = null,
                selectionArgs = null,
                sortOrder = "${DbTableCategory.COLUMN_NAME_TITLE} ASC"
            )
        }

        fun cardBalanceQuery(cardUUID: String): Query {
            return Query(
                selection = "${DbTableOperation.COLUMN_NAME_CARD_UUID} = ?",
                selectionArgs = arrayOf(cardUUID),
                sortOrder = null
            )
        }

        fun operationByCategoryQuery(categoryUUID: String): Query {
            return Query(
                selection = "${DbTableOperation.COLUMN_NAME_CATEGORY_UUID} = ?",
                selectionArgs = arrayOf(categoryUUID),
                sortOrder = null
            )
        }

        fun defaultForCards(): Query {
            return Query(
                selection = "${DbTableCards.COLUMN_NAME_VISIBLE} = ?",
                selectionArgs = arrayOf("1"),
                sortOrder = "${DbTableCards.COLUMN_NAME_VISIBLE} DESC, ${DbTableCards.COLUMN_NAME_TITLE} ASC"
            )
        }

        fun queryForDeletedCard(transaction : OperationClass): Query?{
            val cardUUID = CommentUtils.decodeComment(transaction.comment) ?: return null

            return Query(
                selection = "${DbTableOperation.COLUMN_NAME_TYPE} = ? " +
                        "AND ${DbTableOperation.COLUMN_NAME_DATE} = ? " +
                        "AND ${DbTableOperation.COLUMN_NAME_MONEY} = ? " +
                        "AND ${DbTableOperation.COLUMN_NAME_CARD_UUID} = ?",
                selectionArgs = arrayOf(Constance.plus, transaction.date, transaction.money.toString(), cardUUID),
                sortOrder = null
            )
        }
    }
}

class SettingsQueryBuilder(private val settings: Settings, private val context: Context) {

    val stringNames = StringNames(context)

    fun build(): Query {

        val conditions = mutableListOf(
            "${DbTableOperation.COLUMN_NAME_DATE} BETWEEN ? AND ?"
        )
        val args = mutableListOf(settings.dateStart, settings.dateEnd)

        // Включаем только выбранные типы (OR внутри скобок)
        settings.operationTypes?.takeIf { it.isNotEmpty() }?.let { list ->
            val subConditions = mutableListOf<String>()
            for (operationType in list) {
                when (operationType) {
                    Constance.admission,
                    Constance.expense,
                    Constance.minus,
                    Constance.plus -> {
                        subConditions.add("${DbTableOperation.COLUMN_NAME_TYPE} = ?")
                        args.add(operationType)
                    }
                }
            }
            if (subConditions.isNotEmpty()) {
                conditions.add("(" + subConditions.joinToString(" OR ") + ")")
            }
        }

        // Исключаем типы (OR внутри, но всё это в AND)
        settings.operationTypesExclude?.takeIf { it.isNotEmpty() }?.let { list ->
            val subConditions = mutableListOf<String>()
            for (operationType in list) {
                when (operationType) {
                    Constance.admission,
                    Constance.expense,
                    Constance.minus,
                    Constance.plus -> {
                        subConditions.add("${DbTableOperation.COLUMN_NAME_TYPE} != ?")
                        args.add(operationType)
                    }
                }
            }
            if (subConditions.isNotEmpty()) {
                conditions.add("(" + subConditions.joinToString(" OR ") + ")")
            }
        }

        // Исключаем типы (OR внутри, но всё это в AND)
        settings.categoryIDs?.takeIf { it.isNotEmpty() }?.let { list ->
            val subConditions = mutableListOf<String>()
            for (categoryUUID in list) {
                subConditions.add("${DbTableOperation.COLUMN_NAME_CATEGORY_UUID} = ?")
                args.add(categoryUUID.UUID)
            }
            if (subConditions.isNotEmpty()) {
                conditions.add("(" + subConditions.joinToString(" OR ") + ")")
            }
        }

        return Query(
            selection = conditions.joinToString(" AND "),
            selectionArgs = args.toTypedArray(),
            sortOrder = settings.orderBy
        )
    }


    fun buildByCategory(): Query {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        settings.operationTypes?.let {
            for (operationType in settings.operationTypes) {
                when (operationType) {
                    Constance.admission,
                    Constance.expense,
                    Constance.minus,
                    Constance.plus-> {
                        conditions.add("${DbTableCategory.COLUMN_NAME_TYPE} = ?")
                        args.add(operationType)
                    }
                }
            }
        }
        return Query(
            selection = conditions.joinToString(" OR "),
            selectionArgs = args.toTypedArray(),
            sortOrder = null
        )
    }

    fun buildForBalance(cardUUIDs: List<String>): Query {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        for (cardUUID in cardUUIDs) {
            conditions.add("${DbTableOperation.COLUMN_NAME_CARD_UUID} = ?")
            args.add(cardUUID)
        }

        return Query(
            selection = conditions.joinToString(" OR "),
            selectionArgs = args.toTypedArray(),
            sortOrder = settings.orderBy
        )
    }

    fun buildForCategoryByList(checkedList: ArrayList<String>): Query {
        val conditions = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()
        checkedList.forEach { item ->
            conditions.add("${DbTableCategory.COLUMN_NAME_TYPE} = ?")
            when (item) {
                stringNames.EXPENSE_OP -> selectionArgs.add(Constance.expense)
                stringNames.ADMISSION_OP -> selectionArgs.add(Constance.admission)
            }
        }

        val query = Query(
            selection = conditions.joinToString(" OR "),
            selectionArgs = selectionArgs.toTypedArray(),
            sortOrder = "${DbTableCategory.COLUMN_NAME_TITLE} ASC"
        )

        return query
    }
}