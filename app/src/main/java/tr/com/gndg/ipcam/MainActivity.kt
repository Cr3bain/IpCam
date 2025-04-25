package tr.com.gndg.ipcam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import tr.com.gndg.ipcam.server.MJPEGStreamer
import tr.com.gndg.ipcam.ui.CameraPreview
import tr.com.gndg.ipcam.ui.theme.IpCamTheme
import tr.com.gndg.ipcam.ui.utils.getLocalIpAddress

class MainActivity : ComponentActivity() {

    private lateinit var streamer: MJPEGStreamer
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.entries.all { it.value }
                if (granted) {
                    initializeStreamer()
                } else {
                    Toast.makeText(this, "Tüm izinler gerekli", Toast.LENGTH_SHORT).show()
                }
            }

        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_WIFI_STATE
        )

        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            initializeStreamer()
        } else {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    private fun initializeStreamer() {
        streamer = MJPEGStreamer(8080)

        coroutineScope.launch {
            try {
                streamer.start()
                Log.d("MJPEG", "Streamer started at port 8080")

                withContext(Dispatchers.Main) {
                    setContent {
                        IpCamTheme {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                CameraStreamScreen(streamer)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MJPEG", "Error starting streamer", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Sunucu başlatılamadı", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        try {
            streamer.stop()
        } catch (e: Exception) {
            Log.e("MJPEG", "Streamer stop error", e)
        }
    }
}

@Composable
fun CameraStreamScreen(streamer: MJPEGStreamer) {
    val context = LocalContext.current

    val ip = getLocalIpAddress(context)

    Column(modifier = Modifier.fillMaxSize()) {
        CameraPreview(streamer)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Yayın bağlantısı: http://${ip}:8080/",
            modifier = Modifier.padding(16.dp)
        )
    }
}
