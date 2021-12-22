package com.example.stalkr.utils;

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

class ImageUtils {
    companion object {
        fun urlImageToImageView(url: String, imageView: ImageView, context: Context) {
            Glide.with(context)
                    .load(url)
                    .into(imageView)
        }
    }
}