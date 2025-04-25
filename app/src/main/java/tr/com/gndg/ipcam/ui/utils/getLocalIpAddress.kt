package tr.com.gndg.ipcam.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress

@SuppressLint("ServiceCast")
fun getLocalIpAddress(context: Context): String {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ipInt = wifiManager.connectionInfo.ipAddress

    val ipBytes = byteArrayOf(
        (ipInt and 0xff).toByte(),
        (ipInt shr 8 and 0xff).toByte(),
        (ipInt shr 16 and 0xff).toByte(),
        (ipInt shr 24 and 0xff).toByte()
    )

    return try {
        InetAddress.getByAddress(ipBytes).hostAddress ?: "0.0.0.0"
    } catch (e: Exception) {
        "0.0.0.0"
    }
}