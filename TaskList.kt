package com.example.todolist

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Lista de tarefas com suporte a drag and drop
@Composable
fun TaskList(
    tarefas: List<String>,
    onDeleteTask: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onReorderTasks: (Int, Int) -> Unit,
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragPosition by remember { mutableStateOf(0f) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.SurfaceDark),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(tarefas) { index, tarefa ->
                val isDragging = draggedIndex == index
                val isTarget = targetIndex == index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTarget && draggedIndex != null) 100.dp else 60.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    draggedIndex = index
                                    targetIndex = index
                                    val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                                    dragPosition = (itemInfo?.offset?.toFloat() ?: 0f) + offset.y
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDrag = { _, dragAmount ->
                                    dragPosition += dragAmount.y

                                    // Encontra o novo target baseado na posição do drag
                                    val itemHeight = with(density) { 64.dp.toPx() }
                                    val currentTargetIndex = ((dragPosition - listState.layoutInfo.viewportStartOffset) / itemHeight).toInt()
                                        .coerceIn(0, tarefas.size - 1)

                                    if (currentTargetIndex != targetIndex) {
                                        targetIndex = currentTargetIndex
                                    }

                                    // Auto-scroll
                                    scope.launch {
                                        when {
                                            dragPosition < 100 -> listState.scrollBy(-20f)
                                            dragPosition > listState.layoutInfo.viewportEndOffset - 100 -> listState.scrollBy(20f)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggedIndex?.let { from ->
                                        targetIndex?.let { to ->
                                            if (from != to) {
                                                onReorderTasks(from, to)
                                            }
                                        }
                                    }
                                    draggedIndex = null
                                    targetIndex = null
                                    dragPosition = 0f
                                }
                            )
                        }
                ) {
                    if (!isDragging) {
                        TaskCard(
                            tarefa = tarefa,
                            isDragging = false,
                            cardColor = if (index % 2 == 0) AppColors.DarkBlue else AppColors.LightBlue,
                            onDeleteClick = { onDeleteTask(tarefa) },
                            onTaskClick = { onTaskClick(tarefa) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isTarget && draggedIndex != null && draggedIndex != index) 0.3f else 1f)
                        )
                    }
                }
            }
        }

        // Overlay do item sendo arrastado
        draggedIndex?.let { index ->
            if (index < tarefas.size) {
                Box(
                    modifier = Modifier
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                0,
                                (dragPosition - listState.firstVisibleItemScrollOffset).toInt()
                            )
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            scaleX = AppConstants.DRAG_SCALE
                            scaleY = AppConstants.DRAG_SCALE
                            shadowElevation = 16f
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

// Card individual de tarefa
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
            .clickable(enabled = !isDragging) { onTaskClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.primaryContainer
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
            Text(
                text = tarefa,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontSize = 18.sp
            )

            IconButton(
                onClick = onDeleteClick,
                enabled = !isDragging,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "✖",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDragging) {
                        Color.White.copy(alpha = 0.3f)
                    } else {
                        AppColors.ErrorRed
                    }
                )
            }
        }
    }
}
