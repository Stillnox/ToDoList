package com.example.todolist

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

// Composable principal do aplicativo
@Composable
fun ToDoApp() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Estado principal do app
    var appState by remember { mutableStateOf(TaskState()) }

    // Carrega tarefas ao iniciar
    LaunchedEffect(Unit) {
        appState = loadTasksFromStorage(sharedPreferences)
    }

    // Launchers para salvar/carregar arquivos
    val fileLaunchers = rememberFileLaunchers(
        context = context,
        appState = appState,
        sharedPreferences = sharedPreferences,
        onStateUpdate = { appState = it }
    )

    // Interface principal
    MainLayout(
        appState = appState,
        onStateUpdate = { appState = it },
        sharedPreferences = sharedPreferences,
        fileLaunchers = fileLaunchers
    )

    // Diálogos
    ShowDialogs(
        appState = appState,
        onStateUpdate = { appState = it },
        sharedPreferences = sharedPreferences,
        context = context
    )
}