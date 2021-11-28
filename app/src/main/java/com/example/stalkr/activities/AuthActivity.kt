package com.example.stalkr.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentTransaction
import com.example.stalkr.MainActivity
import com.example.stalkr.R
import com.example.stalkr.fragments.LoginFragment
import com.example.stalkr.fragments.RegistrationFragment
import com.example.stalkr.interfaces.AuthFragmentCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.example.stalkr.data.UserData
import com.google.firebase.firestore.SetOptions

class AuthActivity : AppCompatActivity(), AuthFragmentCallback {
    private lateinit var fragmentTransaction: FragmentTransaction
    private val loginFragment = LoginFragment()
    private val registrationFragment = RegistrationFragment()

    companion object AuthData {
        val db = FirebaseFirestore.getInstance()
        val userCollectionRef = db.collection("users")
        val groupCollectionRef = db.collection("groups")
        val userDbData = Firebase.auth.currentUser
        var userData = UserData("", "")
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
                userCollectionRef.document(documents.first().id).set(userActive, SetOptions.merge())

                // Update the currentUser model
                userData.updateUserFromDB(firebaseAuth.currentUser!!.uid)
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