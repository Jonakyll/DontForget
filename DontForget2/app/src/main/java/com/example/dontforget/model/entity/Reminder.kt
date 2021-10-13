package com.example.dontforget.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "hour")
    val hour: String,

    @ColumnInfo(name = "frequency")
    val frequency: Int,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "added")
    val added: String,
)
