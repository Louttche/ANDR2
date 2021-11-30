package com.example.stalkr

import android.app.Activity
import android.content.Context
import android.util.Log

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.stalkr.data.InfoWindowData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {
    var mContext = context

    override fun getInfoContents(marker: Marker): View {
        var mInfoView = (mContext as Activity).layoutInflater.inflate(R.layout.view_user_map_overlay, null)
        //var mInfoWindow: InfoWindowData? = marker?.tag as InfoWindowData?

        val tv_username : TextView = mInfoView.findViewById(R.id.tv_user_name)
        tv_username.text = marker.title

        // TODO: Display actual photo
        val iv_photo : ImageView = mInfoView.findViewById(R.id.iv_user_photo)
        iv_photo.setImageDrawable(mContext.getDrawable(R.drawable.ic_baseline_person_24))

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