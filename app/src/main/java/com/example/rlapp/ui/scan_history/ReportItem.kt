package com.example.rlapp.ui.scan_history

data class ReportItem(
    val _id: String,
    val title: String,
    val date: String,
    val type: String,
    val expired: Boolean,
    val isActive: Boolean,
    val createdAt: String
)