package com.dcf.tracker.model

data class DailyLog(
    val date: String,
    val entries: Map<String, Int>,
    val dailyCO2: Double
)