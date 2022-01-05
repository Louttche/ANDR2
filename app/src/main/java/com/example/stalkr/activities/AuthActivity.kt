package com.example.stalkr.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentTransaction
import com.example.stalkr.R
import com.example.stalkr.fragments.login.LoginFragment
import com.example.stalkr.fragments.RegistrationFragment
import com.example.stalkr.interfaces.AuthFragmentCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity(), AuthFragmentCallback {
    private lateinit var fragmentTransaction: FragmentTransaction
    private val loginFragment = LoginFragment()
    private val registrationFragment = RegistrationFragment()

    override fun onStart() {
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
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

    override fun onButtonClickShowRegistration() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.fragmentContainerViewAuth,
            registrationFragment,
            "RegistrationFragment"
        )
        fragmentTransaction.commit()
    }

    override fun onButtonClickShowLogin() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerViewAuth, loginFragment, "LoginFragment")
        fragmentTransaction.commit()
    }

    override fun onAuthenticationComplete() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}