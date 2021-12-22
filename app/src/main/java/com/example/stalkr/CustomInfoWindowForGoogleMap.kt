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
        var mInfoView = (mContext as Activity).layoutInflater.inflate(R.layout.view_user_map_overlay, null)
        //var mInfoWindow: InfoWindowData? = marker?.tag as InfoWindowData?

        val tv_username : TextView = mInfoView.findViewById(R.id.tv_user_name)
        tv_username.text = marker.title

        val urlImage = marker.snippet
        val iv_photo : ImageView = mInfoView.findViewById(R.id.iv_user_photo)
        ImageUtils.urlImageToImageView(urlImage!!, iv_photo, this.mContext)

        val btn_viewprofile : Button = mInfoView.findViewById(R.id.btn_view_profile)
        btn_viewprofile.setOnClickListener{
            // TODO: View this user's profile
            Log.d("wow", "Clicked on view user's profile button")
        }

        val btn_invitetogroup : Button = mInfoView.findViewById(R.id.btn_invite_group)
        btn_invitetogroup.setOnClickListener{
            // TODO: Invite this user to a group
            Log.d("wow", "Clicked on view invite user button")
        }

        return mInfoView
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}