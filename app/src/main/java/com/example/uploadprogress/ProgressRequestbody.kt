package com.example.uploadprogress

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val callback: ProgressCallback
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L

        fileInputStream.use { input ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (input.read(buffer).also { read = it } != -1) {
                handler.post { callback.onProgress(uploaded, contentLength()) }
                uploaded += read
                sink.write(buffer, 0, read)
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}

interface ProgressCallback {
    fun onProgress(bytesUploaded: Long, totalBytes: Long)
}
