package com.buggedlab.pocketops.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buggedlab.pocketops.core.model.NotificationCategory

@Entity(tableName = "app_configs")
data class AppConfigEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isProtected: Boolean = false,
    val isMutedInWorkMode: Boolean = false,
    val isDenylistedFromAiControl: Boolean = false,
    val categoryOverride: NotificationCategory? = null
)
