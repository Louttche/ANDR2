package com.example.stalkr

import android.app.Activity
import android.content.Context
import android.util.Log

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.stalkr.data.InfoWindowData
import com.example.stalkr.utils.ImageUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {
    val storage = Firebase.storage
    var storageRef = storage.reference
    var mContext = context

    override fun getInfoContents(marker: Marker): View {
        // If this also returns null, then the default info window will be used.

        var mInfoView = (mContext as Activity).layoutInflater.inflate(R.layout.view_user_map_overlay, null)
        //var mInfoWindow: InfoWindowData? = marker?.tag as InfoWindowData?

        val tv_username : TextView = mInfoView.findViewById(R.id.tv_user_name)
        tv_username.text = marker.title

        val urlImage = marker.snippet
        val iv_photo : ImageView = mInfoView.findViewById(R.id.iv_user_photo)
        ImageUtils.urlImageToImageView(urlImage!!, iv_photo, this.mContext)

        return mInfoView
    }

    override fun getInfoWindow(marker: Marker): View? {
        // The API will first call getInfoWindow(Marker) and if null is returned, it will then call getInfoContents(Marker)
        return null
    }
}