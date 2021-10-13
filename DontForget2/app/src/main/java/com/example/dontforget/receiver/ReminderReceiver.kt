package com.example.dontforget.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dontforget.R
import com.example.dontforget.view.MainActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val reminderMessage = intent.extras!!.getString("message")
            val reminderId = intent.extras!!.getInt("id")

            val i = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)

            val builder = NotificationCompat.Builder(context!!, "reminder_id")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(reminderMessage)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)

            notificationManager.notify(reminderId, builder.build())
        }
    }
}