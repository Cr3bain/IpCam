package tr.com.gndg.ipcam

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tr.com.gndg.ipcam.utils.MyLifecycleObserver
import tr.com.gndg.ipcam.utils.ObserverListenerInterface
import tr.com.gndg.ipcam.server.MJPEGStreamer
import tr.com.gndg.ipcam.ui.CameraPreview
import tr.com.gndg.ipcam.ui.theme.IpCamTheme
import tr.com.gndg.ipcam.utils.getLocalIpAddress

class MainActivity : ComponentActivity(), ObserverListenerInterface{

    private lateinit var streamer: MJPEGStreamer
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var observer : MyLifecycleObserver

    override fun multiplePermission(arrayMap: Map<String, Boolean>) {
        arrayMap.forEach { (permissionName, isGranted) ->
            Toast.makeText(this, "$permissionName ${if (isGranted) "granted" else "denied"}", Toast.LENGTH_LONG).show()
        }

        if (arrayMap.values.all { it }) {
            initializeStreamer()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observer = MyLifecycleObserver(activityResultRegistry, this)
        lifecycle.addObserver(observer)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                if (observer.hasPermissions(this@MainActivity)) {
                    initializeStreamer()
                } else {
                    observer.permissionRequest()
                }
            }
        }
    }

    private fun initializeStreamer() {
        val context = applicationContext
        val port = 8080
        streamer = MJPEGStreamer(port)

        coroutineScope.launch {
            try {

                withContext(Dispatchers.Main) {
                    setContent {
                        IpCamTheme {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                CameraStreamScreen(streamer, modifier = Modifier, port = port)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MJPEG", "Error starting streamer", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, context.getString(R.string.serverNotStarted), Toast.LENGTH_SHORT)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraStreamScreen(streamer: MJPEGStreamer, modifier: Modifier, port: Int) {
    val context = LocalContext.current

    val ip = getLocalIpAddress(context)

    var openScreen by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {

        ListItem(headlineContent = {
            Text(
                text = stringResource(R.string.cameraIp, ip, port),
                style = MaterialTheme.typography.bodyMedium)
        },
            leadingContent = {
                if (streamer.isAlive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            },
            trailingContent = {
                Switch(checked = openScreen, onCheckedChange = {
                    if (openScreen) {
                        streamer.stop()
                    } else {
                        streamer.start()
                    }
                    openScreen = it
                })
            })

        if (openScreen) {
            CameraPreview(
                streamer = streamer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
