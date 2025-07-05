package com.example.todolist

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Composable que gerencia todos os diálogos
@Composable
fun ShowDialogs(
    appState: TaskState,
    onStateUpdate: (TaskState) -> Unit,
    sharedPreferences: SharedPreferences,
    context: Context
) {
    // Diálogo Adicionar Tarefa
    if (appState.showAddDialog) {
        AddTaskDialog(
            tarefaTexto = appState.taskText,
            onTextChange = {
                onStateUpdate(appState.copy(taskText = it))
            },
            onConfirm = {
                onStateUpdate(addTask(
                    appState.taskText,
                    appState.selectedCategory,
                    appState,
                    sharedPreferences
                ))
            },
            onDismiss = {
                onStateUpdate(appState.copy(showAddDialog = false))
            }
        )
    }

    // Diálogo Confirmar Exclusão
    if (appState.showDeleteDialog && appState.taskToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                appState.taskToDelete?.let { task ->
                    onStateUpdate(deleteTask(
                        task,
                        appState.selectedCategory,
                        appState,
                        sharedPreferences
                    ))
                }
            },
            onDismiss = {
                onStateUpdate(appState.copy(
                    showDeleteDialog = false,
                    taskToDelete = null
                ))
            }
        )
    }

    // Diálogo Detalhes da Tarefa
    if (appState.showDetailsDialog) {
        TaskDetailsDialog(
            tarefa = appState.selectedTask ?: "",
            onDismiss = {
                onStateUpdate(appState.copy(showDetailsDialog = false))
            },
            onShowNotification = {
                appState.selectedTask?.let { task ->
                    showTaskNotification(context, task)
                }
            }
        )
    }
}

// Diálogo para adicionar nova tarefa
@Composable
private fun AddTaskDialog(
    tarefaTexto: String,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Tarefa", color = Color.White) },
        text = {
            TextField(
                value = tarefaTexto,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Digite sua tarefa") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.ButtonBlue)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancelar")
            }
        },
        containerColor = AppColors.SurfaceDark
    )
}

// Diálogo de confirmação de exclusão
@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar exclusão", color = Color.White) },
        text = { Text("Deseja realmente excluir esta tarefa?", color = Color.White) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.ErrorRed)
            ) {
                Text("Sim")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Não")
            }
        },
        containerColor = AppColors.SurfaceDark
    )
}

// Diálogo de detalhes da tarefa
@Composable
private fun TaskDetailsDialog(
    tarefa: String,
    onDismiss: () -> Unit,
    onShowNotification: () -> Unit,
) {
    var notificacaoAtivada by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalhes da Tarefa", color = Color.White) },
        text = {
            Column {
                Text(tarefa, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = notificacaoAtivada,
                        onCheckedChange = { isChecked ->
                            notificacaoAtivada = isChecked
                            if (isChecked) {
                                onShowNotification()
                                Toast.makeText(context, "Notificação ativada!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mostrar nas Notificações", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.ButtonBlue)
            ) {
                Text("Fechar")
            }
        },
        containerColor = AppColors.SurfaceDark
    )
}