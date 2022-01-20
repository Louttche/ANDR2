package com.example.stalkr.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.stalkr.AuthUserObject
import com.example.stalkr.databinding.FragmentAccountBinding
import com.example.stalkr.fragments.MapFragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference

class ImageUtils {
    companion object {
        fun urlImageToImageView(url: String, imageView: ImageView, context: Context) {
            Glide.with(context)
                .load(url)
                .into(imageView)
        }

        fun uploadImage(
            context: Context,
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
            storageRef: StorageReference,
            uid: String,
            binding: ViewBinding,
            userCollectionRef: CollectionReference
        ) {
            // Profile image upload
            if (requestCode == MapFragment.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                // Get the Uri of data
                val filePath = data.data
                if (filePath != null) {
                    val profileImageRef = storageRef.child("profileImages/$uid")

                    profileImageRef.putFile(filePath).addOnSuccessListener {
                        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                            val userProfileImageURL = hashMapOf(
                                "profileImageURL" to uri.toString()
                            )

                            val userQuery = userCollectionRef
                                .whereEqualTo("uid", uid)
                                .get()
                            userQuery.addOnSuccessListener {
                                try {
                                    AuthUserObject.pfpURL = uri.toString()
                                    userCollectionRef.document(it.first().id)
                                        .set(userProfileImageURL, SetOptions.merge())
                                } catch (e: NoSuchElementException) {
                                    Log.d(ContentValues.TAG, "No such element - $e")
                                }

                                if (binding is FragmentAccountBinding) {
                                    // Get new image
                                    if (AuthUserObject != null) {
                                        urlImageToImageView(
                                            AuthUserObject.pfpURL,
                                            binding.ivAvatarAccount,
                                            context
                                        )
                                    }
                                }

                                Toast.makeText(context, "Image Uploaded!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }
}