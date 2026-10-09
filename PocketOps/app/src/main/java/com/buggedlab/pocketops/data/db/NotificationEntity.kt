package com.buggedlab.pocketops.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.buggedlab.pocketops.core.model.NotificationCategory

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["postTimeMillis"]),
        Index(value = ["category"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationKey: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String? = null,
    val category: NotificationCategory,
    val postTimeMillis: Long,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val channelId: String? = null,
    val isProtected: Boolean = false,
    val isSuppressedByRoutine: Boolean = false
)
