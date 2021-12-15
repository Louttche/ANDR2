package com.example.stalkr.activities

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentTransaction
import com.example.stalkr.MainActivity
import com.example.stalkr.R
import com.example.stalkr.AuthUserObject
import com.example.stalkr.fragments.login.LoginFragment
import com.example.stalkr.fragments.RegistrationFragment
import com.example.stalkr.interfaces.AuthFragmentCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.SetOptions

class AuthActivity : AppCompatActivity(), AuthFragmentCallback {
    private lateinit var fragmentTransaction: FragmentTransaction
    private val loginFragment = LoginFragment()
    private val registrationFragment = RegistrationFragment()

    companion object AuthData {
        val db = FirebaseFirestore.getInstance()
        val userDbData = Firebase.auth.currentUser
    }

    override fun onStart() {
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            onAuthenticationComplete()
        }

        super.onStart()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        // Disable night mode (looks weird with the black background)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerViewAuth, loginFragment, "LoginFragment")
        fragmentTransaction.commit()
    }

    private fun setUserData() {
        val firebaseAuth = Firebase.auth
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("uid", firebaseAuth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                // Set this user as active in DB
                val userActive = hashMapOf("isActive" to true)
                db.collection("users").document(documents.first().id).set(userActive, SetOptions.merge())

                // Update user object
                AuthUserObject.uid = documents.first().data["uid"].toString()
                AuthUserObject.name = documents.first().data["name"].toString()
                AuthUserObject.pfpURL = documents.first().data["profileImageURL"].toString()
                AuthUserObject.isActive = documents.first().data["isActive"].toString().toBoolean()

                Log.d(TAG, AuthUserObject.uid)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    override fun onButtonClickShowRegistration() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerViewAuth, registrationFragment, "RegistrationFragment")
        fragmentTransaction.commit()
    }

    override fun onButtonClickShowLogin() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerViewAuth, loginFragment, "LoginFragment")
        fragmentTransaction.commit()
    }

    override fun onAuthenticationComplete() {
        setUserData()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}