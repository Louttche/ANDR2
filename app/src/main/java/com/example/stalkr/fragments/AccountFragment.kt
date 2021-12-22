package com.example.stalkr.fragments

import android.os.Bundle
import com.example.stalkr.databinding.FragmentAccountBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.stalkr.AuthUserObject
import com.example.stalkr.R
import com.example.stalkr.databinding.FragmentMapBinding
import com.example.stalkr.utils.ImageUtils

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        if (AuthUserObject != null){
            binding.tvUsernameAccount.text = AuthUserObject.name
            ImageUtils.urlImageToImageView(AuthUserObject.pfpURL, binding.ivAvatarAccount, requireContext())
        }

        return binding.root
    }
}