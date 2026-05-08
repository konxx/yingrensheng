package com.yingrensheng.core.common.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Formatters {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    fun formatShortTime(instant: Instant): String {
        return dateTimeFormatter.format(instant.atZone(ZoneId.systemDefault()))
    }
}

