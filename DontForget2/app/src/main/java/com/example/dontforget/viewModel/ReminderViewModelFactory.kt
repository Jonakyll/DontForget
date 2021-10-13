package com.example.dontforget.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dontforget.model.repository.ReminderRepository
import java.lang.IllegalArgumentException

class ReminderViewModelFactory(private val reminderRepository: ReminderRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java))
            return ReminderViewModel(reminderRepository) as T
        throw IllegalArgumentException("class ViewModel unknown")
    }
}