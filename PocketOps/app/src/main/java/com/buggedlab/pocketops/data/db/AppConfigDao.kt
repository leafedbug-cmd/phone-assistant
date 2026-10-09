package com.buggedlab.pocketops.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: AppConfigEntity)

    @Query("SELECT * FROM app_configs WHERE packageName = :packageName")
    suspend fun getByPackage(packageName: String): AppConfigEntity?

    @Query("SELECT * FROM app_configs")
    fun observeAll(): Flow<List<AppConfigEntity>>

    @Query("SELECT * FROM app_configs WHERE isProtected = 1")
    suspend fun getProtectedApps(): List<AppConfigEntity>

    @Query("SELECT * FROM app_configs WHERE isDenylistedFromAiControl = 1")
    suspend fun getDenylistedApps(): List<AppConfigEntity>
}
