package com.example.myapplication.core.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatDate(date: Date?): String {
        if (date == null) return "Unknown"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    fun relativeTime(date: Date?): String {
        date ?: return ""
        val diff = System.currentTimeMillis() - date.time
        val mins = diff / 60000
        return when {
            mins < 1 -> "Just now"
            mins < 60 -> "$mins min ago"
            mins < 1440 -> "${mins / 60} hr ago"
            else -> "${mins / 1440} d ago"
        }
    }
}
