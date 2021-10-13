package com.example.dontforget.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dontforget.model.entity.Reminder
import com.example.dontforget.model.repository.ReminderRepository
import kotlinx.coroutines.launch

class ReminderViewModel(private val reminderRepository: ReminderRepository): ViewModel() {

    val reminders = reminderRepository.allReminders
    var addedId: Int = 0

    fun insertReminder(reminder: Reminder) = viewModelScope.launch {
        val idNewReminder = reminderRepository.insertReminder(reminder)
        addedId = idNewReminder.toInt()
    }

    fun updateReminder(reminder: Reminder) = viewModelScope.launch {
        val nbLines = reminderRepository.updateReminder(reminder)
    }

    fun deleteOne(reminder: Reminder) = viewModelScope.launch {
        val nbLines = reminderRepository.deleteOne(reminder)
    }

    fun deleteAll() = viewModelScope.launch {
        val nbLines = reminderRepository.deleteAll()
    }
}