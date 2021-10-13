package com.example.dontforget.model.repository

import com.example.dontforget.model.dao.ReminderDAO
import com.example.dontforget.model.entity.Reminder

class ReminderRepository(private val dao: ReminderDAO) {

    suspend fun insertReminder(reminder: Reminder) = dao.insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) = dao.updateReminder(reminder)

    suspend fun deleteOne(reminder: Reminder) = dao.deleteOne(reminder)

    suspend fun deleteAll() = dao.deleteAll()

    val allReminders = dao.getAll()
}