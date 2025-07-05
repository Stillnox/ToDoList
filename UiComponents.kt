package com.example.todolist

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

// Tabs de categorias
@Composable
fun CategoryTabs(
    categorias: List<String>,
    categoriaSelecionada: Int,
    onCategoriaSelected: (Int) -> Unit,
) {
    TabRow(
        selectedTabIndex = categoriaSelecionada,
        modifier = Modifier.fillMaxWidth(),
        containerColor = AppColors.TabBackground,
        contentColor = Color.White
    ) {
        categorias.forEachIndexed { index, categoria ->
            Tab(
                selected = categoriaSelecionada == index,
                onClick = { onCategoriaSelected(index) },
                text = {
                    Text(
                        text = categoria,
                        color = if (categoriaSelecionada == index) Color.White else Color(0xFF9E9E9E)
                    )
                },
                modifier = Modifier.background(
                    if (categoriaSelecionada == index) {
                        AppColors.SurfaceDark
                    } else {
                        Color(0xFF2A2A2A)
                    }
                )
            )
        }
    }
}

// Botões de ação flutuantes
@Composable
fun ActionButtons(
    onAddClick: () -> Unit,
    onSaveClick: () -> Unit,
    onLoadClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Botão Adicionar (canto direito)
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = AppColors.ButtonBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp)
                .offset(0.dp, (-30).dp)
        ) {
            Text("+", color = Color.White, fontSize = 24.sp)
        }

        // Botão Salvar (canto esquerdo)
        FloatingActionButton(
            onClick = onSaveClick,
            containerColor = AppColors.ButtonBlue,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(26.dp)
                .offset(0.dp, (-30).dp)
        ) {
            Text("Salvar", color = Color.White)
        }

        // Botão Carregar (centro-esquerda)
        FloatingActionButton(
            onClick = onLoadClick,
            containerColor = AppColors.ButtonBlue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(26.dp)
                .offset((-60).dp, (-30).dp)
        ) {
            Text("Carregar", color = Color.White)
        }
    }
}

// Botões de email
@Composable
fun EmailButtons(
    categorias: List<String>,
    categoriaSelecionada: Int,
    listaTarefasAtual: List<String>,
    context: Context
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Botão Enviar Backup
        Button(
            onClick = {
                val tarefasTexto = listaTarefasAtual.joinToString("\n")
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "Backup das Tarefas - ${categorias[categoriaSelecionada]}")
                    putExtra(Intent.EXTRA_TEXT, tarefasTexto)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.ButtonBlue)
        ) {
            Text("Enviar Backup por Email")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão Carregar do Email
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "text/*"
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.ButtonBlue)
        ) {
            Text("Carregar Tarefas do Email")
        }
    }
}