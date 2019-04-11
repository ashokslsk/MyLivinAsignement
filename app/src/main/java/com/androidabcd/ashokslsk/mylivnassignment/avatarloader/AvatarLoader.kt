package com.androidabcd.ashokslsk.mylivnassignment.avatarloader

import android.content.Context
import android.widget.ImageView

/**
 * Created by Srinivasa Ashok Kumar on 11/04/19.
 */
interface AvatarLoader {

    fun of(context: Context): AvatarLoader {
        return AvatarLoaderImpl[context]
    }

    fun into(imageView: ImageView): AvatarLoader?

    fun attachPlaceholder(placeholderRes: Int): AvatarLoader?

    fun errorPlaceholder(errorPlaceholderRes: Int): AvatarLoader?

    fun isCircular(isCircular: Boolean): AvatarLoader?

    fun callback(callback: AvatarCallBack): AvatarLoader?

    fun enableCaching(cacheSizeInMB: Int): AvatarLoader?

    fun load(imageUrl: String)
}
