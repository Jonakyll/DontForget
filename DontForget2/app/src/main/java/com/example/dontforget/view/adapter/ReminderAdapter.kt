package com.example.dontforget.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dontforget.databinding.ReminderLayoutBinding
import com.example.dontforget.model.entity.Reminder

class ReminderAdapter(
    private val reminders: List<Reminder>,
    private val listener: OnElementClickListener
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(
        private val context: Context,
        private val binding: ReminderLayoutBinding,
        private val listener: OnElementClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun adapteView(reminder: Reminder) {
            binding.tvReminderMessage.text = reminder.message
            binding.tvReminderHour.text = reminder.hour
            binding.tvReminderDate.text = reminder.date
            binding.clReminder.setOnClickListener {
                listener.setOnElementClickListener(reminder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ReminderLayoutBinding.inflate(inflater, parent, false)
        return ReminderViewHolder(parent.context, binding, listener)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.adapteView(reminders[position])
    }

    override fun getItemCount(): Int {
        return reminders.size
    }
}

interface OnElementClickListener {
    fun setOnElementClickListener(reminder: Reminder)
}