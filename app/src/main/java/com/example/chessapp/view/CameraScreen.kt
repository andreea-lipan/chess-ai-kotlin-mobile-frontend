package com.example.chessapp.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.material.icons.filled.Add
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageCaptured(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bottom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery button, as a future addition
            // Requires refactoring of how the corners are capture
            // Rn they are correlated to the screen size anf not the picture (if the picture is smaller)
//            FloatingActionButton(
//                onClick = { galleryLauncher.launch("image/*") },
//                containerColor = MaterialTheme.colorScheme.secondaryContainer
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Favorite,
//                    contentDescription = "Select from Gallery"
//                )
//            }

            // Capture button (larger, centered)
            FloatingActionButton(
                onClick = {
                    val outputDirectory = context.cacheDir
                    val photoFile = File(
                        outputDirectory,
                        "chessboard_${System.currentTimeMillis()}.jpg"
                    )

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    cameraController.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(Uri.fromFile(photoFile))
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Capture",
                    modifier = Modifier.size(32.dp)
                )
            }

//            // Spacer to balance the layout (invisible button)
//            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}
//
//@Composable
//fun CameraScreen(
//    onImageCaptured: (Uri) -> Unit,
//    onError: (Exception) -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    val cameraController = remember {
//        LifecycleCameraController(context).apply {
//            bindToLifecycle(lifecycleOwner)
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        AndroidView(
//            factory = { ctx ->
//                PreviewView(ctx).apply {
//                    controller = cameraController
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        Button(
//            onClick = {
//                val outputDirectory = context.cacheDir
//                val photoFile = File(
//                    outputDirectory,
//                    "chessboard_${System.currentTimeMillis()}.jpg"
//                )
//
//                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//                cameraController.takePicture(
//                    outputOptions,
//                    ContextCompat.getMainExecutor(context),
//                    object : ImageCapture.OnImageSavedCallback {
//                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                            onImageCaptured(Uri.fromFile(photoFile))
//                        }
//
//                        override fun onError(exception: ImageCaptureException) {
//                            onError(exception)
//                        }
//                    }
//                )
//            },
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(16.dp)
//        ) {
//            Text("Capture")
//        }
//    }
//}