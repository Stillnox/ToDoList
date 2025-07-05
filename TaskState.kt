package com.example.todolist

// Modelo de dados principal do aplicativo
data class TaskState(
    val personalTasks: List<String> = emptyList(),
    val workTasks: List<String> = emptyList(),
    val otherTasks: List<String> = emptyList(),
    val selectedCategory: Int = 0,
    val taskText: String = "",
    val showAddDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showDetailsDialog: Boolean = false,
    val taskToDelete: String? = null,
    val selectedTask: String? = null
) {
    // Função auxiliar para obter tarefas da categoria atual
    fun getCurrentTasks(): List<String> = when (selectedCategory) {
        0 -> personalTasks
        1 -> workTasks
        else -> otherTasks
    }
}