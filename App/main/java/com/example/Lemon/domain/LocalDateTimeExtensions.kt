package com.example.arcus.domain

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



val LocalDateTime.hourStringInTwelveHourFormat: String
    get() {

        val dateTimeFormatter = DateTimeFormatter.ofPattern("hh a")

        return format(dateTimeFormatter).let {
            if (it.startsWith("0")) it.replaceFirst('0', ' ')
            else it
        }
    }