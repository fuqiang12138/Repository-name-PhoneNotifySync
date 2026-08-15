package com.example.phonenotifysync.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationInfo(
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
