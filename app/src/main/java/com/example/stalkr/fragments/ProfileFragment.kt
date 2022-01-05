package com.example.stalkr.fragments

import android.content.ContentValues
import android.os.Bundle
import android.security.ConfirmationCallback
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.stalkr.databinding.FragmentProfileBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stalkr.utils.ImageUtils
import androidx.annotation.NonNull

import androidx.fragment.app.FragmentResultListener
import androidx.navigation.fragment.navArgs
import com.example.stalkr.AuthUserObject
import com.example.stalkr.MapFragment
import com.example.stalkr.MapFragmentDirections
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileFragmentArgs by navArgs<ProfileFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var uID = args.userID
        Log.d("wow", "User - $uID")

        val users = AuthActivity.db.collection("users")
        val userQuery = users
            .whereEqualTo("uid", uID)
            .get()
        userQuery.addOnSuccessListener {
            try {
                var document : QueryDocumentSnapshot = it.first()

                var name = document.data["name"].toString()
                var pfpURL = document.data["profileImageURL"].toString()

                binding.tvUsernameContentProfile.text = name
                ImageUtils.urlImageToImageView(pfpURL, binding.ivAvatarProfile, requireContext())

            } catch (e: NoSuchElementException) {
                Log.d(ContentValues.TAG, "No such element - $e")
            }
        }
    }
}