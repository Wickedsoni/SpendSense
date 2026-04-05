package com.wickedcoder.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun currentMonthRange(): Pair<Long, Long> = monthRange(0)
    fun lastMonthRange(): Pair<Long, Long>    = monthRange(-1)

    private fun monthRange(monthOffset: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59);      cal.set(Calendar.MILLISECOND, 999)
        return start to cal.timeInMillis
    }

    fun formatAmount(amount: Double): String = "₹%,.0f".format(amount)

    fun formatTimestamp(timestamp: Long): String =
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))

    fun formatDay(timestamp: Long): String =
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))

    /** Returns the hour-of-day (0–23) from a timestamp */
    fun hourOf(timestamp: Long): Int =
        Calendar.getInstance().also { it.timeInMillis = timestamp }.get(Calendar.HOUR_OF_DAY)
}
