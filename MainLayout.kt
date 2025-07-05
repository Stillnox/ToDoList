package com.example.todolist

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Layout principal do aplicativo
@Composable
fun MainLayout(
    appState: TaskState,
    onStateUpdate: (TaskState) -> Unit,
    sharedPreferences: SharedPreferences,
    fileLaunchers: FileLaunchers
) {
    val categorias = listOf("Pessoal", "Trabalho", "Outros")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundDark)
            .offset(0.dp, (-5).dp)
            .padding(8.dp)
    ) {
        // Tabs de categorias
        CategoryTabs(
            categorias = categorias,
            categoriaSelecionada = appState.selectedCategory,
            onCategoriaSelected = {
                onStateUpdate(appState.copy(selectedCategory = it))
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Área principal com lista e botões
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.SurfaceDark)
        ) {
            // Lista de tarefas
            TaskList(
                tarefas = appState.getCurrentTasks(),
                onDeleteTask = { tarefa ->
                    onStateUpdate(appState.copy(
                        taskToDelete = tarefa,
                        showDeleteDialog = true
                    ))
                },
                onTaskClick = { tarefa ->
                    onStateUpdate(appState.copy(
                        selectedTask = tarefa,
                        showDetailsDialog = true
                    ))
                },
                onReorderTasks = { fromIndex, toIndex ->
                    onStateUpdate(reorderTasks(
                        fromIndex, toIndex,
                        appState.selectedCategory,
                        appState,
                        sharedPreferences
                    ))
                }
            )

            // Botões de ação flutuantes
            ActionButtons(
                onAddClick = {
                    onStateUpdate(appState.copy(showAddDialog = true))
                },
                onSaveClick = fileLaunchers.onSave,
                onLoadClick = fileLaunchers.onLoad
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botões de email
        EmailButtons(
            categorias = categorias,
            categoriaSelecionada = appState.selectedCategory,
            listaTarefasAtual = appState.getCurrentTasks(),
            context = LocalContext.current
        )
    }
}