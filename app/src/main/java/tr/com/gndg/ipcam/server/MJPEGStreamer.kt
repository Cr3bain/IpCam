package tr.com.gndg.ipcam.server

import android.graphics.Bitmap
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.concurrent.thread

class MJPEGStreamer(port: Int) : NanoHTTPD(port) {

    private val boundary = "boundary"
    @Volatile private var currentBitmap: Bitmap? = null
    //private val listeners = mutableListOf<(Bitmap) -> Unit>()

    override fun serve(session: IHTTPSession?): Response {
        val pipedInput = PipedInputStream()
        val pipedOutput = PipedOutputStream(pipedInput)

        // Başlıkları ayarla
        val headers = Response.Status.OK to "multipart/x-mixed-replace; boundary=$boundary"

        // MJPEG stream yazan thread
        thread {
            try {
                while (true) {
                    val bitmap = currentBitmap ?: continue
                    val jpeg = encodeBitmapToJPEG(bitmap)

                    pipedOutput.write("--$boundary\r\n".toByteArray())
                    pipedOutput.write("Content-Type: image/jpeg\r\n".toByteArray())
                    pipedOutput.write("Content-Length: ${jpeg.size}\r\n\r\n".toByteArray())
                    pipedOutput.write(jpeg)
                    pipedOutput.write("\r\n".toByteArray())
                    pipedOutput.flush()

                    Thread.sleep(100) // FPS ayarı (10 kare/saniye gibi düşün)
                }
            } catch (_: IOException) {
                // istemci bağlantısı kesildi
                pipedOutput.close()
            }
        }

        return newChunkedResponse(headers.first, headers.second, pipedInput)
    }

    fun updateBitmap(bitmap: Bitmap) {
        currentBitmap = bitmap
    }

    private fun encodeBitmapToJPEG(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
}
