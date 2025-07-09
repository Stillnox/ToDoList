package com.example.todolist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    // Função principal que é executada quando a activity é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cria o canal de notificação necessário para Android 8.0+
        createNotificationChannel()

        // Define o conteúdo da tela usando Jetpack Compose
        setContent {
            AppTheme {
                ToDoApp()
            }
        }
    }

    // Cria um canal de notificação para permitir que o app envie notificações
    // Obrigatório para Android 8.0 (API 26) e superiores
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AppConstants.NOTIFICATION_CHANNEL_ID,
            AppConstants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = AppConstants.NOTIFICATION_CHANNEL_DESC
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
