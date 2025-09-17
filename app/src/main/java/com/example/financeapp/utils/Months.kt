package com.example.financeapp.utils

import com.example.financeapp.ui.common.LanguageManager
import com.example.financeapp.utils.Constance.DEFAULT
import com.example.financeapp.utils.Constance.LANGUAGE_ENG
import com.example.financeapp.utils.Constance.LANGUAGE_RU
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

enum class MonthNamesENG(val fullName: String,
                         val shortName: String)
{
    JAN("January", "Jan"),
    FEB("February", "Feb"),
    MAR("March", "Mar"),
    APR("April", "Apr"),
    MAY("May", "May"),
    JUN("June", "Jun"),
    JUL("July", "Jul"),
    AUG("August", "Aug"),
    SEP("September", "Sep"),
    OCT("October", "Oct"),
    NOV("November", "Nov"),
    DEC("December", "Dec");

    companion object {
        fun fromNumber(month: Int): MonthNamesENG {
            return values()[month - 1]
        }
    }
}

enum class MonthNamesRU(val fullName: String,
                        val shortName: String)
{
    JAN("Январь", "янв"),
    FEB("Февраль", "февр"),
    MAR("Март", "март"),
    APR("Апрель", "апр"),
    MAY("Май", "май"),
    JUN("Июнь", "июнь"),
    JUL("Июль", "июль"),
    AUG("Август", "авг"),
    SEP("Сентябрь", "сент"),
    OCT("Октябрь", "окт"),
    NOV("Ноябрь", "нояб"),
    DEC("Декабрь", "дек");

    companion object {
        fun fromNumber(month: Int): MonthNamesRU {
            return values()[month - 1]
        }
    }
}

object Months {

    fun getShortNameOfMonth(): Map<String, String>{
        val language = LanguageManager.getLanguage()

        return when (language){
            DEFAULT, LANGUAGE_ENG -> MonthNamesENG.entries.associate { it.fullName to it.shortName}
            LANGUAGE_RU -> MonthNamesRU.entries.associate { it.fullName to it.shortName}
            else -> MonthNamesENG.entries.associate { it.fullName to it.shortName}
        }
    }

    fun getNameOfMonth(date: String): String {
        val monthValue = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).monthValue
        val language = LanguageManager.getLanguage()

        return when (language){
            DEFAULT, LANGUAGE_ENG -> getNameOfMonthEng(monthValue)
            LANGUAGE_RU -> getNameOfMonthRus(monthValue)
            else -> getNameOfMonthEng(monthValue)
        }
    }


    private fun getNameOfMonthRus(monthValue: Int): String {
        return MonthNamesRU.fromNumber(monthValue).fullName
    }

    private fun getNameOfMonthEng(monthValue: Int): String {
        return MonthNamesENG.fromNumber(monthValue).fullName
    }

    fun getMonthRange(monthName: String, yearStartDate: String): Pair<String, String>? {
        val formatter = DateUtils.formatterSQL
        val year = DateUtils.getYear(yearStartDate)

        val monthIndex = when (LanguageManager.getLanguage()) {
            DEFAULT, LANGUAGE_ENG -> MonthNamesENG.entries
                .find { it.fullName.equals(monthName, ignoreCase = true) }?.ordinal ?: -1

            LANGUAGE_RU -> MonthNamesRU.entries
                .find { it.fullName.equals(monthName, ignoreCase = true) }?.ordinal ?: -1

            else -> MonthNamesENG.entries
                .find { it.fullName.equals(monthName, ignoreCase = true) }?.ordinal ?: -1
        }

        if (monthIndex == -1) return null

        val yearMonth = YearMonth.of(year, monthIndex + 1)
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()

        return Pair(firstDay.format(formatter), lastDay.format(formatter))
    }


}