package com.example.dontforget.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dontforget.model.dao.ReminderDAO
import com.example.dontforget.model.entity.Reminder

@Database(entities = [Reminder::class], version = 1)
abstract class DB: RoomDatabase() {

    abstract val reminderDao: ReminderDAO

    companion object {
        @Volatile
        private var INSTANCE: DB? = null

        fun getInstance(context: Context) : DB {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context, DB::class.java, "DBReminders").build()
                }
                return instance
            }
        }
    }
}