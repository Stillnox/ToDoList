package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// Tema customizado do aplicativo
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val customColorScheme = darkColorScheme(
        primary = AppColors.ButtonBlue,
        primaryContainer = AppColors.DarkBlue,
        background = AppColors.BackgroundDark,
        surface = AppColors.SurfaceDark,
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = customColorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundDark)
        ) {
            content()
        }
    }
}