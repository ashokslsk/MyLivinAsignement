package com.androidabcd.ashokslsk.mylivnassignment.avatarloader

import android.content.Context
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.http.HttpResponseCache
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference


/**
 * Created by Srinivasa Ashok Kumar on 11/04/19.
 */
internal class AvatarLoaderImpl : AvatarLoader, CallBacks {


    private val weakContext: WeakReference<Context>? = null
    private val avatarLoader: AvatarLoader? = null

    private var avatarDownloadTask: AvatarDownloader? = null
    private var callback: AvatarCallBack? = null
    private var weakImageView = WeakReference<ImageView>(null)
    private var isCircular = false
    private var isCachingEnabled = false
    private var bitmapLoadingPlaceholder: Bitmap? = null
    private var bitmapErrorPlaceholder: Bitmap? = null


    companion object {
        private val TAG = "AvatarLoader"
        private var weakContext: WeakReference<Context>? = null
        private var avatarLoader: AvatarLoader? = null

        operator fun get(context: Context): AvatarLoader {
            weakContext = WeakReference(context)
            if (avatarLoader == null) {
                avatarLoader = AvatarLoaderImpl()
            }
            return avatarLoader as AvatarLoader
        }
    }

    override fun isCircular(isCircular: Boolean): AvatarLoader? {
        this.isCircular = isCircular
        return avatarLoader
    }

    override fun into(imageView: ImageView): AvatarLoader? {
        weakImageView = WeakReference(imageView)
        return avatarLoader
    }

    override fun attachPlaceholder(placeholderRes: Int): AvatarLoader? {
        this.bitmapLoadingPlaceholder =
            BitmapFactory.decodeResource(weakContext?.get()?.resources, placeholderRes)
        return avatarLoader
    }

    override fun errorPlaceholder(errorPlaceholderRes: Int): AvatarLoader? {
        this.bitmapErrorPlaceholder =
            BitmapFactory.decodeResource(weakContext?.get()?.resources, errorPlaceholderRes)
        return avatarLoader
    }

    override fun callback(callback: AvatarCallBack): AvatarLoader? {
        this.callback = callback
        return avatarLoader
    }

    override fun enableCaching(cacheSizeInMB: Int): AvatarLoader? {
        this.isCachingEnabled = true
        enableHttpCaching(cacheSizeInMB)
        return avatarLoader
    }

    private fun enableHttpCaching(cacheSizeInMB: Int) {
        if (weakContext?.get() == null)
            return
        try {
            val httpCacheDir = File(weakContext?.get()?.cacheDir, "http")
            val httpCacheSize = (cacheSizeInMB * 1024 * 1024).toLong()
            HttpResponseCache.install(httpCacheDir, httpCacheSize)
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }

    }

    override fun load(avatarUrl: String) {
        if (avatarDownloadTask != null) {
            avatarDownloadTask?.cancel(true)
            avatarDownloadTask = null
        }
        avatarDownloadTask = AvatarDownloader(this, isCachingEnabled)
            .execute(avatarUrl)
    }

    override fun onProgress(progress: Int?) {
        if (callback != null) {
            progress?.let { callback?.onProgress(it) }
        }
    }

    override fun onStart() {
        attachBitmap(bitmapLoadingPlaceholder)
    }


    override fun onError(e: Exception) {
        if (callback != null) {
            callback?.onError(e)
        }
        attachBitmap(bitmapErrorPlaceholder)
    }

    override fun onCancelled() {
        if (callback != null) {
            callback?.onCancelled()
        }
    }

    override fun onAvatarAvailable(bitmap: Bitmap) {
        if (callback != null) {
            callback?.onAvatarAvailable(bitmap)
        }
        attachBitmap(bitmap)
    }

    private fun attachBitmap(bitmap: Bitmap?) {
        val imageView: ImageView? = weakImageView.get()
        if (imageView != null && weakContext?.get() != null) {
            if (isCircular && bitmap != null) {
                imageView?.setImageDrawable(cropToCircle(cropToSquare(bitmap)))
            } else {
                imageView?.setImageBitmap(bitmap)
            }
        } else {
            Log.e(TAG, "Missing ImageView or Context!")
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = if (height > width) width else height
        val newHeight = if (height > width) height - (height - width) else height

        var cropW = (width - height) / 2
        cropW = if (cropW < 0) 0 else cropW

        var cropH = (height - width) / 2
        cropH = if (cropH < 0) 0 else cropH

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight)
    }

    private fun cropToCircle(bitmap: Bitmap): RoundedBitmapDrawable? {
        val bitmapDrawable = weakContext?.get()?.resources?.let { RoundedBitmapDrawableFactory.create(it, bitmap) }
        bitmapDrawable?.isCircular = true
        return bitmapDrawable
    }
}
