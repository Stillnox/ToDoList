package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Lista de tarefas com suporte a drag and drop
@Composable
fun TaskList(
    tarefas: List<String>,
    onDeleteTask: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onReorderTasks: (Int, Int) -> Unit,
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOffset by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(AppColors.SurfaceDark),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        itemsIndexed(tarefas) { index, tarefa ->
            DraggableTaskItem(
                index = index,
                tarefa = tarefa,
                isDragging = draggedIndex == index,
                draggedOffset = if (draggedIndex == index) draggedOffset else 0f,
                onDragStart = {
                    draggedIndex = index
                    draggedOffset = 0f
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDrag = { offset ->
                    draggedOffset += offset

                    // Auto-scroll quando próximo das bordas
                    scope.launch {
                        val itemHeightPx = with(density) { AppConstants.ITEM_HEIGHT_DP.dp.toPx() }
                        val scrollOffset = draggedOffset / itemHeightPx

                        when {
                            scrollOffset > 2f -> listState.scrollBy(itemHeightPx / 10f)
                            scrollOffset < -2f -> listState.scrollBy(-itemHeightPx / 10f)
                        }
                    }
                },
                onDragEnd = {
                    draggedIndex?.let { currentIndex ->
                        val itemHeightPx = with(density) { AppConstants.ITEM_HEIGHT_DP.dp.toPx() }
                        val numberOfItems = (draggedOffset / itemHeightPx).roundToInt()
                        val targetIndex = (currentIndex + numberOfItems)
                            .coerceIn(0, tarefas.size - 1)

                        if (currentIndex != targetIndex) {
                            onReorderTasks(currentIndex, targetIndex)
                        }
                    }
                    draggedIndex = null
                    draggedOffset = 0f
                },
                onDeleteClick = { onDeleteTask(tarefa) },
                onTaskClick = { onTaskClick(tarefa) }
            )
        }
    }
}

// Item de tarefa arrastável
@Composable
private fun DraggableTaskItem(
    index: Int,
    tarefa: String,
    isDragging: Boolean,
    draggedOffset: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDeleteClick: () -> Unit,
    onTaskClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .graphicsLayer {
                translationY = draggedOffset
                scaleX = if (isDragging) AppConstants.DRAG_SCALE else 1f
                scaleY = if (isDragging) AppConstants.DRAG_SCALE else 1f
                alpha = if (isDragging) AppConstants.DRAG_ALPHA else 1f
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, _ -> onDrag(change.position.y) }
                )
            }
            .zIndex(if (isDragging) 1f else 0f)
    ) {
        TaskCard(
            tarefa = tarefa,
            isDragging = isDragging,
            cardColor = if (index % 2 == 0) AppColors.DarkBlue else AppColors.LightBlue,
            onDeleteClick = { if (!isDragging) onDeleteClick() },
            onTaskClick = { if (!isDragging) onTaskClick() }
        )
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
) {
    Card(
        modifier = Modifier
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