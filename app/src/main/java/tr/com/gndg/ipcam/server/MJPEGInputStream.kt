package tr.com.gndg.ipcam.server

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference

// Maybe another day
/*
class MJPEGInputStream(
    private val bitmapRef: AtomicReference<Bitmap?>
) : InputStream() {

    private val boundary = "--frame\r\nContent-Type: image/jpeg\r\n\r\n"
    private var buffer = ByteArray(0)
    private var position = 0

    override fun read(): Int {
        if (position >= buffer.size) {
            val bmp = bitmapRef.get() ?: return -1
            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)

            val frame = boundary.toByteArray() + out.toByteArray() + "\r\n".toByteArray()
            buffer = frame
            position = 0
        }
        return buffer[position++].toInt() and 0xFF
    }
}
*/
