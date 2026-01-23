package com.jetsynthesys.rightlife.ui.utility

fun isValidIndianMobile(number: String): Boolean {
        val trimmed = number.trim()

        // Must be exactly 10 digits
        if (trimmed.length != 10) return false

        // Reject all same digits like 0000000000, 1111111111
        if (trimmed.all { it == trimmed[0] }) return false

        // Must start with 6–9
        val regex = Regex("^[6-9]\\d{9}$")
        return regex.matches(trimmed)
    }