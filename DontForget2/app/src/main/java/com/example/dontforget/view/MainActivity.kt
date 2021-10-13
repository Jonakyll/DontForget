package com.example.dontforget.view

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.UpdateDialogBinding
import com.example.dontforget.model.DB
import com.example.dontforget.model.entity.Reminder
import com.example.dontforget.model.repository.ReminderRepository
import com.example.dontforget.receiver.ReminderReceiver
import com.example.dontforget.view.adapter.OnElementClickListener
import com.example.dontforget.view.adapter.ReminderAdapter
import com.example.dontforget.viewModel.ReminderViewModel
import com.example.dontforget.viewModel.ReminderViewModelFactory
import java.util.*

class MainActivity : AppCompatActivity(), OnElementClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ReminderViewModel
    private lateinit var adapter: ReminderAdapter
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        val dao = DB.getInstance(application).reminderDao
        val repository = ReminderRepository(dao)
        val factory = ReminderViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ReminderViewModel::class.java]

        binding.fabAdd.setOnClickListener {
            showUpdateDialog(null)
        }

        binding.bDeleteAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.button_delete_all))
                .setMessage(resources.getString(R.string.d_delete_all))
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    viewModel.reminders.map {
                        it.forEach { reminder ->
                            deleteReminder(reminder)
                        }
                    }
                    viewModel.deleteAll()
                }
                .setNegativeButton(resources.getString(R.string.no), null)
                .show()
        }

        showReminders()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showUpdateDialog(reminder: Reminder?) {
        val alertDialog = AlertDialog.Builder(this)
        val dialogBinding = UpdateDialogBinding.inflate(layoutInflater)
        alertDialog.setView(dialogBinding.root)
        val dialog = alertDialog.create()

        calendar = Calendar.getInstance()

        dialogBinding.tvHour.setOnClickListener {
            showTimePickerDialog(dialogBinding)
        }

        dialogBinding.rgFrequency.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.rbDaily -> {
                    dialogBinding.tvDate.visibility = View.INVISIBLE
                    dialogBinding.tvReminderDate.visibility = View.INVISIBLE
                }
                else -> {
                    dialogBinding.tvDate.visibility = View.VISIBLE
                    dialogBinding.tvReminderDate.visibility = View.VISIBLE
                }
            }
        }

        dialogBinding.tvDate.setOnClickListener {
            showDatePickerDialog(dialogBinding)
        }

//        add new reminder
        if (reminder == null) {
            dialogBinding.bAdd.text = resources.getString(R.string.button_add)
            dialogBinding.bAdd.setOnClickListener {
                if (!TextUtils.isEmpty(dialogBinding.edMessage.text.toString()) && !TextUtils.isEmpty(
                        dialogBinding.tvReminderHour.text.toString()
                    )
                ) {
                    val date = getDate(calendar, dialogBinding.rgFrequency.checkedRadioButtonId)
                    val newReminder = Reminder(
                        0,
                        dialogBinding.edMessage.text.toString(),
                        dialogBinding.tvReminderHour.text.toString(),
                        dialogBinding.rgFrequency.checkedRadioButtonId,
                        date,
                        Calendar.getInstance().time.toString()
                    )
                    viewModel.insertReminder(newReminder)
                    setReminder(newReminder, true)
                }
                dialog.dismiss()
            }
        } else {
//            update old reminder
            dialogBinding.bDelete.visibility = View.VISIBLE
            dialogBinding.bDelete.setOnClickListener {
                deleteReminder(reminder)
                viewModel.deleteOne(reminder)
                dialog.dismiss()
            }

            dialogBinding.bAdd.text = resources.getString(R.string.button_update)
            dialogBinding.edMessage.setText(reminder.message)
            dialogBinding.tvReminderHour.text = reminder.hour
            dialogBinding.tvReminderDate.text = reminder.date
            dialogBinding.rgFrequency.check(reminder.frequency)
            dialogBinding.bAdd.setOnClickListener {
                if (!TextUtils.isEmpty(dialogBinding.edMessage.text.toString())) {
                    val date = getDate(calendar, dialogBinding.rgFrequency.checkedRadioButtonId)
                    val newReminder = Reminder(
                        reminder.id,
                        dialogBinding.edMessage.text.toString(),
                        dialogBinding.tvReminderHour.text.toString(),
                        dialogBinding.rgFrequency.checkedRadioButtonId,
                        date,
                        Calendar.getInstance().time.toString()
                    )
                    viewModel.updateReminder(newReminder)
                    deleteReminder(reminder)
                    setReminder(newReminder, false)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showReminders() {
        viewModel.reminders.observe(this, {
            adapter = ReminderAdapter(it, this)
            binding.rvReminders.adapter = adapter
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun setOnElementClickListener(reminder: Reminder) {
        showUpdateDialog(reminder)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "reminder_channel"
            val description = "channel for reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("reminder_id", name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setReminder(reminder: Reminder, added: Boolean) {
        val id = if (added) viewModel.addedId else reminder.id
        when (reminder.frequency) {
            R.id.rbOnce -> {
                alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, ReminderReceiver::class.java)
                intent.putExtra("id", id)
                intent.putExtra("message", reminder.message)

                pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)

                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            R.id.rbDaily -> {
                alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, ReminderReceiver::class.java)
                intent.putExtra("id", id)
                intent.putExtra("message", reminder.message)

                pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent
                )
            }
            R.id.rbWeekly -> {
                alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, ReminderReceiver::class.java)
                intent.putExtra("id", id)
                intent.putExtra("message", reminder.message)

                pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7, pendingIntent
                )
            }
        }
    }

    private fun deleteReminder(reminder: Reminder) {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("id", reminder.id)
        intent.putExtra("message", reminder.message)

        pendingIntent = PendingIntent.getBroadcast(this, reminder.id, intent, 0)

        alarmManager.cancel(pendingIntent)
    }

    private fun getDate(calendar: Calendar, checkedId: Int): String {
        return when (checkedId) {
            R.id.rbOnce -> calendar[Calendar.DAY_OF_MONTH].toString() + "/" + calendar[Calendar.MONTH].toString() + "/" + calendar[Calendar.YEAR].toString()
            R.id.rbDaily -> resources.getString(R.string.rb_daily)
            R.id.rbWeekly -> getDayName(calendar.get(Calendar.DAY_OF_WEEK))
            else -> ""
        }
    }

    private fun getDayName(dayCode: Int): String {
        return when (dayCode) {
            Calendar.MONDAY -> resources.getString(R.string.monday)
            Calendar.TUESDAY -> resources.getString(R.string.tuesday)
            Calendar.WEDNESDAY -> resources.getString(R.string.wednesday)
            Calendar.THURSDAY -> resources.getString(R.string.thursday)
            Calendar.FRIDAY -> resources.getString(R.string.friday)
            Calendar.SATURDAY -> resources.getString(R.string.saturday)
            Calendar.SUNDAY -> resources.getString(R.string.sunday)
            else -> ""
        }
    }

    private fun showTimePickerDialog(dialogBinding: UpdateDialogBinding) {
        val timePicker = TimePickerDialog(
            this,
            { _, hour, minute ->
                dialogBinding.tvReminderHour.text = "$hour:$minute"
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
            }, 12, 0, true
        )
        timePicker.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDatePickerDialog(dialogBinding: UpdateDialogBinding) {
        when (dialogBinding.rgFrequency.checkedRadioButtonId) {
            R.id.rbOnce -> {
                val datePicker = DatePickerDialog(this)
                datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = month
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    dialogBinding.tvReminderDate.text = getDate(
                        calendar,
                        dialogBinding.rgFrequency.checkedRadioButtonId
                    )
                }
                datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
                datePicker.show()
            }
            R.id.rbWeekly -> {
                val datePicker = DatePickerDialog(this)
                datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = month
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    dialogBinding.tvReminderDate.text = getDate(
                        calendar,
                        dialogBinding.rgFrequency.checkedRadioButtonId
                    )
                }
                datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
                datePicker.show()
            }
            R.id.rbDaily -> {
                calendar[Calendar.YEAR] = Calendar.getInstance()[Calendar.YEAR]
                calendar[Calendar.MONTH] = Calendar.getInstance()[Calendar.YEAR]
                calendar[Calendar.DAY_OF_MONTH] = Calendar.getInstance()[Calendar.YEAR]
            }
        }
    }

}