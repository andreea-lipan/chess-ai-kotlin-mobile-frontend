package com.example.chessapp

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chessapp.view.CameraScreen
import com.example.chessapp.view.ChessboardCorners
import com.example.chessapp.view.CornerSelectionScreen
import com.example.chessapp.view.ResultScreen

@Composable
fun ChessboardAppRouter() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Camera) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCorners by remember { mutableStateOf<ChessboardCorners?>(null) }

    when (currentScreen) {
        Screen.Camera -> {
            CameraScreen(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    currentScreen = Screen.CornerSelection
                },
                onError = { exception ->
                    // Handle error
                }
            )
        }
        Screen.CornerSelection -> {
            capturedImageUri?.let { uri ->
                CornerSelectionScreen(
                    imageUri = uri,
                    onCornersConfirmed = { corners ->
                        selectedCorners = corners
                        currentScreen = Screen.Result
                    },
                    onBack = {
                        capturedImageUri = null
                        selectedCorners = null
                        currentScreen = Screen.Camera
                    }
                )
            }
        }
        Screen.Result -> {
            selectedCorners?.let { corners ->
                ResultScreen(
                    corners = corners,
                    onRetry = {
                        // Reset and go back to camera
                        capturedImageUri = null
                        selectedCorners = null
                        currentScreen = Screen.Camera
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object Camera : Screen()
    object CornerSelection : Screen()
    object Result : Screen()
}