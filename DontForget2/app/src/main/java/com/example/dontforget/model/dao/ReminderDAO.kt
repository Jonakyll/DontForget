package com.example.dontforget.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.dontforget.model.entity.Reminder

@Dao
interface ReminderDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder): Int

    @Delete
    suspend fun deleteOne(reminder: Reminder): Int

    @Query(value = "DELETE FROM reminders")
    suspend fun deleteAll(): Int

    @Query(value = "SELECT * FROM reminders")
    fun getAll(): LiveData<List<Reminder>>
}