package tr.com.gndg.ipcam.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface ObserverListenerInterface {
    fun multiplePermission(arrayMap : Map<String, Boolean>)
}

val requiredPermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_WIFI_STATE
)

class MyLifecycleObserver(private val registry : ActivityResultRegistry, private val listenerInterface: ObserverListenerInterface)
    : DefaultLifecycleObserver {
    lateinit var multiplePermissionRequest : ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        multiplePermissionRequest = registry.register("permission", ActivityResultContracts.RequestMultiplePermissions()
        ) {
            listenerInterface.multiplePermission(it)
        }
    }

    fun hasPermissions(context: Context): Boolean = requiredPermissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun permissionRequest(array: Array<String> = requiredPermissions) {
        multiplePermissionRequest.launch(array)
    }
}