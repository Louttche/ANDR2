package com.example.stalkr.fragments

import android.content.Intent
import android.os.Bundle
import com.example.stalkr.databinding.FragmentAccountBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.stalkr.AuthUserObject
import com.example.stalkr.R
import com.example.stalkr.utils.ImageUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!


    // AUTH + DB
    private val userCollectionRef = FirebaseFirestore.getInstance().collection("users")
    private val storageRef = FirebaseStorage.getInstance().reference

    // temp - for debug
    private var uid: String = Firebase.auth.currentUser?.uid.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        if (AuthUserObject != null) {
            binding.tvUsernameAccount.text = AuthUserObject.name
            ImageUtils.urlImageToImageView(
                AuthUserObject.pfpURL,
                binding.ivAvatarAccount,
                requireContext()
            )
        }

        // Picture upload click handler
        binding.ivAvatarAccount.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Select Image from here..."
                ),
                MapFragment.PICK_IMAGE_REQUEST
            )
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Upload image
        ImageUtils.uploadImage(
            requireContext(),
            requestCode,
            resultCode,
            data,
            storageRef,
            uid,
            binding,
            userCollectionRef
        )
    }
}