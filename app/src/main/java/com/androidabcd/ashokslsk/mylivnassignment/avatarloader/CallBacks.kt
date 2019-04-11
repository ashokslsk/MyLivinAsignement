package com.androidabcd.ashokslsk.mylivnassignment.avatarloader

import android.graphics.Bitmap



/**
 * Created by Srinivasa Ashok Kumar on 11/04/19.
 */
interface CallBacks {
    fun onProgress(progress: Int?)
    fun onStart()
    fun onAvatarAvailable(bitmap: Bitmap)
    fun onError(e: Exception)
    fun onCancelled()
}