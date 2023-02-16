package com.example.runningtracker.db

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var id: Int? = null,
    @ColumnInfo
    var image: Bitmap? = null,
    @ColumnInfo
    var timeStamp: Long? = 0L,
    @ColumnInfo
    var avgSpeedInKMH: Float? = 0f,
    @ColumnInfo
    var distanceInMeters: Int? = 0,
    @ColumnInfo
    var timeInMillis: Long? = 0L,
    @ColumnInfo
    var caloriesBurned: Int? = 0,
)
