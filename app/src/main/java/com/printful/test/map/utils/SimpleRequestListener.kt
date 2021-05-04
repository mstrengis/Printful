package com.printful.test.map.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class SimpleRequestListener(private val onResourceReady: (Bitmap, DataSource) -> Unit) :
    RequestListener<Bitmap> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Bitmap>?,
        isFirstResource: Boolean
    ): Boolean = false

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any?,
        target: Target<Bitmap>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        if (resource != null && dataSource != null) {
            onResourceReady(resource, dataSource)
        }
        return false
    }
}