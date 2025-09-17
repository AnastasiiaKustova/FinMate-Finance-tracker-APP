package com.example.financeapp.utils

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone


object DateUtils {
    val formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatterDisplay = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val tz: ZoneOffset = ZoneOffset.UTC

    private val today: LocalDate
        get() = LocalDate.now()

    val todayDateMillis: Long
        get() = today
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    val startOfMonthDateMillis: Long
        get() = firstDayOfMonth
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    val endOfMonthDateMillis: Long
        get() = firstDayNextMonth.minusDays(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    private val firstDayOfMonth: LocalDate = today.withDayOfMonth(1)
    private val lastDayOfYear: LocalDate = LocalDate.of(today.year, 12, 31)
    private val firstDayOfYear: LocalDate = LocalDate.of(today.year, 1, 1)
    private val firstDayNextMonth: LocalDate = today.plusMonths(1).withDayOfMonth(1)

    private val month = today.month // Получаем объект Month
    val monthNumber = month.value

    fun getCurrentDateStart(): String = firstDayOfMonth.format(formatterSQL)

    fun getCurrentDate(): String = today.format(formatterSQL)

    fun getYesterday(): String = today.minusDays(1).format(formatterSQL)

    fun getPlanningDateEnd(): String = firstDayNextMonth.minusDays(1).format(formatterSQL)

    fun getDateStart(): String = firstDayOfYear.format(formatterSQL)

    fun getDateEnd(): String = lastDayOfYear.format(formatterSQL)

    fun dateSQLtoString(date: String): String =
        runCatching { LocalDate.parse(date, formatterSQL).format(formatterDisplay) }
            .getOrDefault(date)

    fun dateToSQL(date: String): String {
        require(date.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
            "Wrong date format: $date. Require format dd.MM.yyyy"
        }
        return LocalDate.parse(date, formatterDisplay).format(formatterSQL)
    }

    fun dateForLabel(dateStart: String, dateEnd: String): String {
        return "${dateSQLtoString(dateStart)} - ${dateSQLtoString(dateEnd)}"
    }

    fun parseDateToMillis(dateString: String): Long {
        val localDate = LocalDate.parse(dateString, formatterSQL)
        return localDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

    fun parseMillisToString(cal: Calendar): String {

        val localDate = Instant.ofEpochMilli(cal.time.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return formatterSQL.format(localDate)
    }

    fun getYear(yearStartDate: String): Int {
        val parsedDate = LocalDate.parse(yearStartDate, formatterSQL)
        return parsedDate.year
    }

    fun getYearRange(year: Int): Pair<String, String> {
        val firstDay = LocalDate.of(year, 1, 1)
        val lastDay = LocalDate.of(year, 12, 31)

        return Pair(firstDay.format(formatterSQL), lastDay.format(formatterSQL))
    }
}