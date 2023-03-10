package com.example.stalkr

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.stalkr.data.InfoWindowData
import com.example.stalkr.utils.ImageUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import javax.sql.DataSource

class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {
    private var mContext = context

    override fun getInfoContents(marker: Marker): View {
        // If this also returns null, then the default info window will be used.

        var mInfoView = (mContext as Activity).layoutInflater.inflate(R.layout.view_user_map_overlay, null)
        //var mInfoWindow: InfoWindowData? = marker?.tag as InfoWindowData?

        val tv_username : TextView = mInfoView.findViewById(R.id.tv_user_name)
        tv_username.text = marker.title

        val imageUrl = marker.snippet
        val iv_photo : ImageView = mInfoView.findViewById(R.id.iv_user_photo)
        if (imageUrl !== "") {
            Glide.with(this.mContext)
                .load(imageUrl)
                .listener(MarkerCallback(marker))
                .into(iv_photo)
        }

        return mInfoView
    }

    override fun getInfoWindow(marker: Marker): View? {
        // The API will first call getInfoWindow(Marker) and if null is returned, it will then call getInfoContents(Marker)
        return null
    }
}

class MarkerCallback internal constructor(marker: Marker?) :
    RequestListener<Drawable> {

    var marker: Marker? = null

    private fun onSuccess() {
        if (marker != null && marker!!.isInfoWindowShown) {
            marker!!.hideInfoWindow()
            marker!!.showInfoWindow()
        }
    }

    init {
        this.marker = marker
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        Log.e(javaClass.simpleName, "Error loading thumbnail! -> $e")
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<Drawable>?,
        dataSource: com.bumptech.glide.load.DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        onSuccess()
        return false
    }
}