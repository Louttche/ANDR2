package com.example.stalkr.repositories

import android.util.Patterns.EMAIL_ADDRESS
import com.example.stalkr.enums.VerificationType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthRepoImpl : AuthRepo {
    private var isValid = true

    override fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onFinishedListener: AuthRepo.OnFinishedListener
    ) {
        validateCredentials(email, password, onFinishedListener)
        if (isValid) {
            val firebaseAuth = Firebase.auth

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onFinishedListener.onLogin()
                } else {
                    onFinishedListener.onVerificationError(task.exception.toString())
                }
            }

        }
    }

    private fun validateCredentials(
        email: String,
        password: String,
        onFinishedListener: AuthRepo.OnFinishedListener
    ) {
        if (email.isEmpty()) {
            onFinishedListener.onVerificationError(VerificationType.EMPTY_EMAIL)
            isValid = false
        } else if (!EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            onFinishedListener.onVerificationError(VerificationType.INVALID_EMAIL)
            isValid = false
        }
        else {
            onFinishedListener.onValidEmail()
        }
        if (password.isEmpty()) {
            onFinishedListener.onVerificationError(VerificationType.EMPTY_PASSWORD)
            isValid = false
        }
        else {
            onFinishedListener.onValidPassword()
        }
    }
}