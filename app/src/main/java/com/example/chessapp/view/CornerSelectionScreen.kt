package com.example.chessapp.view

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ChessboardCorners(
    val topLeft: Offset,
    val topRight: Offset,
    val bottomRight: Offset,
    val bottomLeft: Offset,
    val imageUri: String,
    val originalImageWidth: Float,
    val originalImageHeight: Float  // to track original dimensions
)

@Composable
fun CornerSelectionScreen(
    imageUri: Uri,
    onCornersConfirmed: (ChessboardCorners) -> Unit,
    onBack: () -> Unit
) {
    var corners by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var imageSize by remember { mutableStateOf<IntSize?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Chessboard",
            onSuccess = { state ->
                // Capture the displayed image size
                imageSize = IntSize(
                    state.painter.intrinsicSize.width.toInt(),
                    state.painter.intrinsicSize.height.toInt()
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (corners.size < 4) {
                            corners = corners + offset
                        }
                    }
                },
            contentScale = ContentScale.Fit
        )

        // Draw corner markers
        Canvas(modifier = Modifier.fillMaxSize()) {
            corners.forEachIndexed { index, corner ->
                drawCircle(
                    color = Color.Red,
                    radius = 20f,
                    center = corner
                )
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = corner
                )
            }

            // Draw lines between corners
            if (corners.size >= 2) {
                for (i in 0 until corners.size - 1) {
                    drawLine(
                        color = Color.Green,
                        start = corners[i],
                        end = corners[i + 1],
                        strokeWidth = 3f
                    )
                }
                if (corners.size == 4) {
                    drawLine(
                        color = Color.Green,
                        start = corners[3],
                        end = corners[0],
                        strokeWidth = 3f
                    )
                }
            }
        }

        // Instructions
        Text(
            text = "Tap to select corners (${corners.size}/4)\n" +
                    "Order: Top-Left → Top-Right → Bottom-Right → Bottom-Left",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp)
                .padding(top = 36.dp),
            color = Color.White
        )

        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { corners = emptyList() }) {
                Text("Reset")
            }

            Button(
                onClick = {
                    imageSize?.let { size ->
                        onCornersConfirmed(
                            ChessboardCorners(
                                topLeft = corners[0],
                                topRight = corners[1],
                                bottomRight = corners[2],
                                bottomLeft = corners[3],
                                imageUri = imageUri.toString(),
                                originalImageWidth = size.width.toFloat(),
                                originalImageHeight = size.height.toFloat()
                            )
                        )
                    }
                },
                enabled = corners.size == 4 && imageSize != null
            ) {
                Text("Confirm")
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
