package com.example.financeapp.data.model

import android.icu.util.ULocale.Category
import android.os.Parcelable
import com.example.financeapp.utils.Constance
import kotlinx.parcelize.Parcelize

open class ClassWithCategory(
    open var categoryUUID: String,
    open var categoryName: String,
)

@Parcelize
data class OperationClass(
    override var categoryUUID: String = "",
    override var categoryName: String = "",
    val UUID: String = "",
    var id: Int = 0,
    var type: String,
    var date: String,
    var comment: String,
    var cardUUID: String,
    var cardName: String = "",
    var cardToName: String = "",
    var money: Int,
) : ClassWithCategory(categoryUUID = categoryUUID, categoryName = categoryName), Parcelable {
    override fun toString(): String = if (comment.isBlank()) type else comment
}

data class OperationClassWithDate(
    var dataList: List<OperationClass>,
    var date: String,
    var info: AdditionalInfo,
)

data class AdditionalInfo(
    var money: Int? = null,
    var categoryUUID: String? = null,
    var categoryName: String? = null,
    var planningMoney: Int? = null,
)

@Parcelize
data class CategoryClass(
    val id: Int,
    val UUID: String = "",
    var title: String,
    var typeOperation: String,
    var iconId: Int,
    var colorId: String,
) : Parcelable {
    override fun toString(): String = title
    companion object {
        fun empty(): CategoryClass {
            return CategoryClass(0, "", "unknown", Constance.expense, 0, "")
        }
    }
}

@Parcelize
data class PlanningClass(
    var id: Int = 0,
    val UUID: String = "",
    override var categoryUUID: String,
    override var categoryName: String = "",
    var planningValue: Int,
    var dateStart: String,
    var dateEnd: String,
    var comment: String,
    var spentValue: Int = 0,
) : ClassWithCategory(categoryUUID = categoryUUID, categoryName = categoryName), Parcelable

@Parcelize
data class CardsClass(
    var id: Int = 0,
    val UUID: String = "",
    var title: String,
    var date: String,
    var visible: Int,
    var comment: String,
    var balance: Int = 0
) : Parcelable {
    override fun toString(): String = title
}

@Parcelize
data class SettingsItemClass(
    var UUID: String,
    var title: String,
    var code: String
) : Parcelable {
    override fun toString(): String = title
}

data class FilterChip(
    var tag: String,
    var title: String
)