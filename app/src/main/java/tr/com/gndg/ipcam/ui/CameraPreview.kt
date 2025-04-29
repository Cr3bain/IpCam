package tr.com.gndg.ipcam.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import tr.com.gndg.ipcam.server.MJPEGStreamer
import java.util.concurrent.Executors

/*fun ImageProxy.toBitmap(): Bitmap? {
    // YUV_420_888 formatındaki görüntüyü NV21'e dönüştürme
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

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)
    val imageBytes = out.toByteArray()

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}*/

@Composable
fun CameraPreview(streamer: MJPEGStreamer,
                  modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = rememberUpdatedState(LocalContext.current as LifecycleOwner).value
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    AndroidView(factory = {
        val previewView = PreviewView(context)
        val executor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                //.setTargetFrameRate(Range(30, 30))
                .build().also {
                    it.surfaceProvider = previewView.surfaceProvider
            }

            // Çözünürlük ve FPS ayarları


            val imageAnalyzer = ImageAnalysis.Builder()
                //.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                it.setAnalyzer(executor) { imageProxy ->
                    try {
                        val bitmap = imageProxy.toBitmap()
                        if (true) {
                            streamer.updateBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Frame error", e)
                    } finally {
                        imageProxy.close()
                    }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(context))

        previewView
    },
        modifier = modifier)
}
