package com.androidabcd.ashokslsk.mylivnassignment.avatarloader

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Srinivasa Ashok Kumar on 11/04/19.
 */

internal class AvatarDownloader(private val callback: CallBacks, private val isCachingEnabled: Boolean) :
    AsyncTask<String, Int, Bitmap>() {

    override fun doInBackground(vararg params: String): Bitmap? {
        return downloadAvatar(params[0])
    }

    override fun onProgressUpdate(vararg values: Int?) {
        callback.onProgress(values[0])
    }

    override fun onPreExecute() {
        callback.onStart()
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        if (bitmap != null) {
            callback.onAvatarAvailable(bitmap)
        } else {
            callback.onError(IOException("Avatar could not be loaded!"))
        }
    }

    override fun onCancelled() {
        callback.onCancelled()
    }

    fun execute(imageUrl: String): AvatarDownloader {
        super.execute(imageUrl)
        return this
    }

    private fun downloadAvatar(imageUrl: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false

        var stream: InputStream? = null
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.useCaches = isCachingEnabled

            stream = connection.inputStream

            val lengthOfFile = connection.contentLength

            val bufLen = 4 * 0x400
            var bytes = ByteArray(bufLen)
            var readLen: Int
            var total: Long = 0
            ByteArrayOutputStream().use { outputStream ->
                readLen = stream.read(bytes, 0, bufLen)
                while (readLen != -1) {
                    outputStream.write(bytes, 0, readLen)
                    total += readLen.toLong()

                    publishProgress((total * 100 / lengthOfFile).toInt())
                }

                bytes = outputStream.toByteArray()
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            return null
        } finally {
            try {
                stream?.close()
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
        }
    }

    companion object {
        private val TAG = "AvatarDownloader"
    }
}