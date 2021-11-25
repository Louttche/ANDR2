package com.example.stalkr

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.stalkr.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // MAIN
    private lateinit var binding: ActivityMainBinding

    // AUTH
    var db: FirebaseFirestore? = FirebaseFirestore.getInstance()
    //private var userName: String = "Sally"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // when app is initially opened the Map Fragment should be visible
        changeFragment(mapFragment())
    }

    // function to change the fragment which is used to reduce the lines of code
    private fun changeFragment(fragmentToChange: Fragment): Unit {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, fragmentToChange)
            addToBackStack(null)
            commit()
        }
    }
}