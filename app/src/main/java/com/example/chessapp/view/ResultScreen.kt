package com.example.chessapp.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.chessapp.service.ChessDetectionService
import com.example.chessapp.service.ChessPositionResponse

@Composable
fun ResultScreen(
    corners: ChessboardCorners,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ChessDetectionService() }
    val uriHandler = LocalUriHandler.current

    var displayedImageSize by remember { mutableStateOf<IntSize?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var chessResponse by remember { mutableStateOf<ChessPositionResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Make API call when screen loads
    LaunchedEffect(corners) {
        isLoading = true
        errorMessage = null

        scope.launch {
            repository.detectChessPosition(context, corners.imageUri.toUri(), corners)
                .onSuccess { response ->
                    chessResponse = response
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Unknown error occurred"
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 36.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chess pieces detection",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display the image with corners marked
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = corners.imageUri.toUri(),
                contentDescription = "Chessboard with corners",
                onSuccess = { state ->
                    displayedImageSize = IntSize(
                        state.painter.intrinsicSize.width.toInt(),
                        state.painter.intrinsicSize.height.toInt()
                    )
                },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Draw corners (same as before)
            displayedImageSize?.let { currentSize ->
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / corners.originalImageWidth
                    val scaleY = size.height / corners.originalImageHeight
                    val scale = minOf(scaleX, scaleY)

                    val displayedWidth = corners.originalImageWidth * scale
                    val displayedHeight = corners.originalImageHeight * scale

                    val offsetX = (size.width - displayedWidth) / 2
                    val offsetY = (size.height - displayedHeight) / 2

                    val cornersList = listOf(
                        corners.topLeft,
                        corners.topRight,
                        corners.bottomRight,
                        corners.bottomLeft
                    )

                    cornersList.forEach { corner ->
                        val scaledCorner = Offset(
                            x = corner.x * scale + offsetX,
                            y = corner.y * scale + offsetY
                        )

                        drawCircle(
                            color = Color.Red,
                            radius = 20f,
                            center = scaledCorner
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 15f,
                            center = scaledCorner
                        )
                    }

                    for (i in cornersList.indices) {
                        val nextIndex = (i + 1) % cornersList.size
                        val scaledStart = Offset(
                            x = cornersList[i].x * scale + offsetX,
                            y = cornersList[i].y * scale + offsetY
                        )
                        val scaledEnd = Offset(
                            x = cornersList[nextIndex].x * scale + offsetX,
                            y = cornersList[nextIndex].y * scale + offsetY
                        )

                        drawLine(
                            color = Color.Green,
                            start = scaledStart,
                            end = scaledEnd,
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator()
            Text(
                text = "Analyzing chessboard...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Chess detection results
        chessResponse?.let { response ->
            // Lichess Link Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(response.lichessUrl) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔗 Open in Lichess",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = response.lichessUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FEN Notation Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FEN Notation:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = response.fen,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Corner coordinates
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Corner Coordinates:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Original Image Size: ${corners.originalImageWidth.toInt()} × ${corners.originalImageHeight.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CornerDataRow("Top Left", corners.topLeft)
                CornerDataRow("Top Right", corners.topRight)
                CornerDataRow("Bottom Right", corners.bottomRight)
                CornerDataRow("Bottom Left", corners.bottomLeft)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Retry button
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture New Image")
        }
    }
}

@Composable
fun CornerDataRow(label: String, offset: Offset) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "X: ${offset.x.toInt()}, Y: ${offset.y.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
