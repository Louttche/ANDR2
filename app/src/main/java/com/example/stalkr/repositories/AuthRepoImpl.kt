package com.example.stalkr.repositories

import android.content.ContentValues
import android.util.Log
import com.example.stalkr.AuthUserObject
import com.example.stalkr.Validation
import com.example.stalkr.enums.VerificationType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class AuthRepoImpl : AuthRepo {
    private val validation = Validation()
    var firebaseAuth = Firebase.auth

    override fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onFinishedListener: AuthRepo.OnFinishedListener
    ) {

        if (validateCredentials(email, password, onFinishedListener)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Set user status to active
                    setUserStatus()
                    onFinishedListener.onLogin()
                } else {
                    onFinishedListener.onVerificationError(task.exception.toString())
                }
            }
        }
    }

    private fun setUserStatus() {
        val firebaseAuth = Firebase.auth
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("uid", firebaseAuth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                // Set this user as active in DB
                val userActive = hashMapOf("isActive" to true)
                FirebaseFirestore.getInstance().collection("users").document(documents.first().id)
                    .set(userActive, SetOptions.merge())

                // Update user object
                AuthUserObject.uid = documents.first().data["uid"].toString()
                AuthUserObject.name = documents.first().data["name"].toString()
                AuthUserObject.pfpURL = documents.first().data["profileImageURL"].toString()
                AuthUserObject.isActive = documents.first().data["isActive"].toString().toBoolean()

                Log.d(ContentValues.TAG, AuthUserObject.uid)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun validateCredentials(
        email: String,
        password: String,
        onFinishedListener: AuthRepo.OnFinishedListener
    ): Boolean {
        var isValid = true

        if (!validation.isEmailValid(email)) {
            if (validation.getIsEmailEmpty()) {
                onFinishedListener.onVerificationError(VerificationType.EMPTY_EMAIL)
            } else {
                onFinishedListener.onVerificationError(VerificationType.INVALID_EMAIL)
            }
            isValid = false
        } else {
            onFinishedListener.onValidEmail()
        }
        if (!validation.isPasswordValid(password)) {
            if (validation.getIsPasswordEmpty()) {
                onFinishedListener.onVerificationError(VerificationType.EMPTY_PASSWORD)
                isValid = false
            } else {
                onFinishedListener.onValidPassword()
            }
        } else {
            onFinishedListener.onValidPassword()
        }
        return isValid
    }

}