package com.example.todolist

import android.content.SharedPreferences
import androidx.core.content.edit

// Adiciona uma nova tarefa
fun addTask(
    taskText: String,
    category: Int,
    state: TaskState,
    sharedPreferences: SharedPreferences
): TaskState {
    if (taskText.isEmpty()) return state

    val updatedState = when (category) {
        0 -> {
            val updatedTasks = state.personalTasks + taskText
            updateTasksInStorage(category, updatedTasks, sharedPreferences)
            state.copy(personalTasks = updatedTasks)
        }
        1 -> {
            val updatedTasks = state.workTasks + taskText
            updateTasksInStorage(category, updatedTasks, sharedPreferences)
            state.copy(workTasks = updatedTasks)
        }
        else -> {
            val updatedTasks = state.otherTasks + taskText
            updateTasksInStorage(category, updatedTasks, sharedPreferences)
            state.copy(otherTasks = updatedTasks)
        }
    }

    return updatedState.copy(taskText = "", showAddDialog = false)
}

// Deleta uma tarefa
fun deleteTask(
    taskToDelete: String,
    category: Int,
    state: TaskState,
    sharedPreferences: SharedPreferences
): TaskState {
    val updatedState = when (category) {
        0 -> {
            val updatedTasks = state.personalTasks.filter { it != taskToDelete }
            updateTasksInStorage(category, updatedTasks, sharedPreferences)
            state.copy(personalTasks = updatedTasks)
        }
        1 -> {
            val updatedTasks = state.workTasks.filter { it != taskToDelete }
            updateTasksInStorage(category, updatedTasks, sharedPreferences)
            state.copy(workTasks = updatedTasks)
        }
        else -> {
            val updatedTasks = state.otherTasks.filter { it != taskToDelete }
            updateTasksInStorage(category, updatedTasks, sharedPreferences)
            state.copy(otherTasks = updatedTasks)
        }
    }

    return updatedState.copy(showDeleteDialog = false, taskToDelete = null)
}

// Reordena tarefas após drag and drop
fun reorderTasks(
    fromIndex: Int,
    toIndex: Int,
    category: Int,
    state: TaskState,
    sharedPreferences: SharedPreferences
): TaskState {
    val currentList = when (category) {
        0 -> state.personalTasks.toMutableList()
        1 -> state.workTasks.toMutableList()
        else -> state.otherTasks.toMutableList()
    }

    if (fromIndex !in currentList.indices || toIndex !in currentList.indices) {
        return state
    }

    val item = currentList.removeAt(fromIndex)
    currentList.add(toIndex, item)

    updateTasksInStorage(category, currentList, sharedPreferences)

    return when (category) {
        0 -> state.copy(personalTasks = currentList)
        1 -> state.copy(workTasks = currentList)
        else -> state.copy(otherTasks = currentList)
    }
}

// Carrega tarefas do SharedPreferences
fun loadTasksFromStorage(sharedPreferences: SharedPreferences): TaskState {
    return TaskState(
        personalTasks = sharedPreferences.getStringSet(AppConstants.PREFS_PERSONAL, setOf())?.toList() ?: listOf(),
        workTasks = sharedPreferences.getStringSet(AppConstants.PREFS_WORK, setOf())?.toList() ?: listOf(),
        otherTasks = sharedPreferences.getStringSet(AppConstants.PREFS_OTHER, setOf())?.toList() ?: listOf()
    )
}

// Atualiza tarefas no SharedPreferences
fun updateTasksInStorage(
    categoria: Int,
    tarefas: List<String>,
    sharedPreferences: SharedPreferences,
) {
    val key = when (categoria) {
        0 -> AppConstants.PREFS_PERSONAL
        1 -> AppConstants.PREFS_WORK
        else -> AppConstants.PREFS_OTHER
    }
    sharedPreferences.edit {
        putStringSet(key, tarefas.toSet())
    }
}