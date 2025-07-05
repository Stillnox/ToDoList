package com.example.todolist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

// Classe para encapsular os launchers de arquivo
data class FileLaunchers(
    val onSave: () -> Unit,
    val onLoad: () -> Unit
)

// Cria os launchers para salvar e carregar arquivos
@Composable
fun rememberFileLaunchers(
    context: Context,
    appState: TaskState,
    sharedPreferences: SharedPreferences,
    onStateUpdate: (TaskState) -> Unit
): FileLaunchers {
    val salvarArquivoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val tarefas = appState.getCurrentTasks()
                        outputStream.write(tarefas.joinToString("\n").toByteArray())
                        Toast.makeText(context, "Tarefas salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val carregarArquivoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val tarefas = inputStream.bufferedReader().readText()
                            .split("\n")
                            .filter { it.isNotBlank() }

                        val newState = when (appState.selectedCategory) {
                            0 -> {
                                updateTasksInStorage(0, tarefas, sharedPreferences)
                                appState.copy(personalTasks = tarefas)
                            }
                            1 -> {
                                updateTasksInStorage(1, tarefas, sharedPreferences)
                                appState.copy(workTasks = tarefas)
                            }
                            else -> {
                                updateTasksInStorage(2, tarefas, sharedPreferences)
                                appState.copy(otherTasks = tarefas)
                            }
                        }

                        onStateUpdate(newState)
                        Toast.makeText(context, "Tarefas carregadas com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao carregar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    return FileLaunchers(
        onSave = {
            try {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, "tarefas.txt")
                }
                salvarArquivoLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao iniciar salvamento: ${e.message}", Toast.LENGTH_LONG).show()
            }
        },
        onLoad = {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                }
                carregarArquivoLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao iniciar carregamento: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    )
}