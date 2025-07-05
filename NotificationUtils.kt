package com.example.todolist

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

// Mostra uma notificação para a tarefa
fun showTaskNotification(context: Context, taskText: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notification = NotificationCompat.Builder(context, AppConstants.NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Tarefa")
        .setContentText(taskText)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    try {
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}