package com.example.preciseroadmap.data.camera

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object SpeedLimitDetector {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun detectSpeedLimit(bitmap: Bitmap): Int? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()

            val text = result.textBlocks.joinToString(" ") { it.text }
            Log.d("OCR", "Detected text: $text")

            // Suche nach typischen Geschwindigkeiten
            val regex = Regex("\\b(30|50|70|100|120)\\b")
            regex.find(text)?.value?.toIntOrNull()
        } catch (e: Exception) {
            Log.e("OCR", "Error detecting speed: ${e.localizedMessage}")
            null
        }
    }
}

package com.example.preciseroadmap.ui

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.preciseroadmap.data.camera.SpeedLimitDetector
import kotlinx.coroutines.*
import java.util.concurrent.Executors

@Composable
fun CameraPreview(onLimitDetected: (Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor, { imageProxy ->
                processImage(imageProxy, onLimitDetected, coroutineScope)
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    ctx as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("Camera", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(ctx))

        previewView
    })
}

fun processImage(
    imageProxy: ImageProxy,
    onLimitDetected: (Int) -> Unit,
    coroutineScope: CoroutineScope
) {
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }

    val bitmap = imageProxy.toBitmap() ?: run {
        imageProxy.close()
        return
    }

    coroutineScope.launch {
        val limit = SpeedLimitDetector.detectSpeedLimit(bitmap)
        if (limit != null) {
            onLimitDetected(limit)
        }
        imageProxy.close()
    }
}

fun ImageProxy.toBitmap(): Bitmap? {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    return try {
        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
        val imageBytes = out.toByteArray()
        android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}

<uses-permission android:name="android.permission.CAMERA"/>

<uses-feature android:name="android.hardware.camera.any"/>

requestPermissions(arrayOf(Manifest.permission.CAMERA), 1002)

var detectedLimit by remember { mutableStateOf<Int?>(null) }

CameraPreview(onLimitDetected = { limit ->
    detectedLimit = limit
})

if (detectedLimit != null) {
    Text("Erkannte Begrenzung: $detectedLimit km/h")
}