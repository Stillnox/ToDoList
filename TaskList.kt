package com.example.todolist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ========== Data classes para gerenciar estado ==========
data class DragState(
    val draggedIndex: Int? = null,
    val targetIndex: Int? = null,
    val dragPosition: Float = 0f,
    val initialOffset: Float = 0f
)

// ========== Componente principal refatorado ==========
// Lista de tarefas com funcionalidade completa de drag and drop
@Composable
fun TaskList(
    tarefas: List<String>,
    onDeleteTask: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onReorderTasks: (Int, Int) -> Unit,
) {
    // Estados para controlar o drag and drop
    var draggedIndex by remember { mutableStateOf<Int?>(null) }     // Índice do item sendo arrastado
    var dragPosition by remember { mutableStateOf(0f) }             // Posição Y atual do drag
    var targetIndex by remember { mutableStateOf<Int?>(null) }      // Índice onde o item será solto
    var initialTouchOffset by remember { mutableStateOf(0f) }       // Offset inicial do toque
    var isDragging by remember { mutableStateOf(false) }            // Flag para controlar se está arrastando

    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current  // Para feedback tátil
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.SurfaceDark),
            contentPadding = PaddingValues(bottom = 120.dp),  // Espaço para os botões flutuantes
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(tarefas) { index, tarefa ->
                val isItemDragging = draggedIndex == index
                val isTarget = targetIndex == index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTarget && draggedIndex != null && draggedIndex != index) 100.dp else 60.dp)  // Aumenta altura do alvo
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .pointerInput(index) {
                            detectDragGesturesAfterLongPress(
                                // Inicia o drag após pressão longa
                                onDragStart = { offset ->
                                    Log.d("DragDrop", "Drag start - Index: $index, Offset: $offset")

                                    // Reset de estados anteriores se houver
                                    if (isDragging) {
                                        Log.d("DragDrop", "Resetting previous drag state")
                                        draggedIndex = null
                                        targetIndex = null
                                        dragPosition = 0f
                                        initialTouchOffset = 0f
                                        isDragging = false
                                    }

                                    draggedIndex = index
                                    targetIndex = index
                                    initialTouchOffset = offset.y
                                    isDragging = true

                                    // Calcula a posição inicial correta baseada no item visível
                                    val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                                    if (itemInfo != null) {
                                        dragPosition = itemInfo.offset.toFloat() + offset.y
                                        Log.d("DragDrop", "Item info found - offset: ${itemInfo.offset}, touchOffset: ${offset.y}, dragPosition: $dragPosition")
                                    } else {
                                        // Fallback se o item não estiver visível
                                        dragPosition = offset.y
                                        Log.d("DragDrop", "Item info not found - using fallback, dragPosition: $dragPosition")
                                    }

                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                // Atualiza posição durante o drag
                                onDrag = { _, dragAmount ->
                                    dragPosition += dragAmount.y

                                    // Calcula novo índice alvo baseado na posição real dos itens visíveis
                                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                                    val listStartOffset = listState.layoutInfo.viewportStartOffset
                                    val adjustedPosition = dragPosition - listStartOffset

                                    // Encontra o item mais próximo da posição atual
                                    var currentTargetIndex = draggedIndex ?: 0

                                    // Se não há itens visíveis, manter o índice atual
                                    if (visibleItems.isNotEmpty()) {
                                        for (item in visibleItems) {
                                            val itemStart = item.offset.toFloat()
                                            val itemEnd = itemStart + item.size.toFloat()

                                            if (adjustedPosition >= itemStart && adjustedPosition <= itemEnd) {
                                                currentTargetIndex = item.index
                                                break
                                            } else if (adjustedPosition < itemStart) {
                                                // Se estamos antes do primeiro item visível, usar o primeiro
                                                currentTargetIndex = item.index
                                                break
                                            } else if (adjustedPosition > itemEnd && item == visibleItems.last()) {
                                                // Se estamos depois do último item visível, usar o último
                                                currentTargetIndex = item.index
                                            }
                                        }
                                    }

                                    // Garante que o índice está dentro dos limites
                                    currentTargetIndex = currentTargetIndex.coerceIn(0, tarefas.size - 1)

                                    if (currentTargetIndex != targetIndex && currentTargetIndex != draggedIndex) {
                                        Log.d("DragDrop", "Target changed: $targetIndex -> $currentTargetIndex, Position: $adjustedPosition")
                                        targetIndex = currentTargetIndex
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else {
                                        Log.d("DragDrop", "Target unchanged: $targetIndex, Current: $currentTargetIndex, Dragged: $draggedIndex, Position: $adjustedPosition")
                                    }

                                    // Auto-scroll quando próximo das bordas
                                    scope.launch {
                                        when {
                                            dragPosition < listStartOffset + 100 -> listState.scrollBy(-20f)
                                            dragPosition > listState.layoutInfo.viewportEndOffset - 100 -> listState.scrollBy(20f)
                                        }
                                    }
                                },
                                // Finaliza o drag e reordena se necessário
                                onDragEnd = {
                                    val from = draggedIndex
                                    val to = targetIndex

                                    Log.d("DragDrop", "Drag end - From: $from, To: $to")

                                    // Reset imediato dos estados
                                    draggedIndex = null
                                    targetIndex = null
                                    dragPosition = 0f
                                    initialTouchOffset = 0f
                                    isDragging = false

                                    // Executa a reordenação se necessário
                                    if (from != null && to != null && from != to) {
                                        Log.d("DragDrop", "Reordering from $from to $to")
                                        Log.d("DragDrop", "Tasks before reorder: $tarefas")
                                        onReorderTasks(from, to)
                                    } else {
                                        Log.d("DragDrop", "No reordering needed - from: $from, to: $to")
                                    }

                                    Log.d("DragDrop", "States reset - draggedIndex: $draggedIndex, targetIndex: $targetIndex, isDragging: $isDragging")
                                }
                            )
                        }
                ) {
                    // Renderiza o card apenas se não estiver sendo arrastado
                    if (!isItemDragging) {
                        TaskCard(
                            tarefa = tarefa,
                            isDragging = false,
                            cardColor = if (index % 2 == 0) AppColors.DarkBlue else AppColors.LightBlue,  // Cores alternadas
                            onDeleteClick = { onDeleteTask(tarefa) },
                            onTaskClick = { onTaskClick(tarefa) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isTarget && draggedIndex != null && draggedIndex != index) 0.3f else 1f)  // Transparência no alvo
                        )
                    }
                }
            }
        }

        // Overlay do item sendo arrastado (flutua sobre a lista)
        draggedIndex?.let { index ->
            if (index < tarefas.size && isDragging) {
                Box(
                    modifier = Modifier
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                0,
                                (dragPosition - initialTouchOffset - listState.firstVisibleItemScrollOffset).toInt()
                            )
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            scaleX = AppConstants.DRAG_SCALE      // Aumenta tamanho durante drag
                            scaleY = AppConstants.DRAG_SCALE
                            shadowElevation = 16f                 // Adiciona sombra
                        }
                ) {
                    TaskCard(
                        tarefa = tarefas[index],
                        isDragging = true,
                        cardColor = if (index % 2 == 0) AppColors.DarkBlue else AppColors.LightBlue,
                        onDeleteClick = {},
                        onTaskClick = {}
                    )
                }
            }
        }
    }
}

// Card individual que representa uma tarefa
@Composable
private fun TaskCard(
    tarefa: String,
    isDragging: Boolean,
    cardColor: Color,
    onDeleteClick: () -> Unit,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isDragging) { onTaskClick() },  // Desabilita clique durante drag
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp      // Maior elevação durante drag
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.primaryContainer          // Cor diferente durante drag
            } else {
                cardColor
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Texto da tarefa
            Text(
                text = tarefa,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontSize = 18.sp
            )

            // Botão de deletar
            IconButton(
                onClick = onDeleteClick,
                enabled = !isDragging,                              // Desabilita durante drag
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "✖",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDragging) {
                        Color.White.copy(alpha = 0.3f)              // Transparente durante drag
                    } else {
                        AppColors.ErrorRed
                    }
                )
            }
        }
    }
}
