package com.example.stalkr.repositories

import com.example.stalkr.Verification
import com.example.stalkr.enums.VerificationType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthRepoImpl : AuthRepo {
    private val verification = Verification()
    var firebaseAuth = Firebase.auth

    override fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onFinishedListener: AuthRepo.OnFinishedListener
    ) {

        if (validateCredentials(email, password, onFinishedListener)) {
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
    ): Boolean {
        var isValid = true

        if (!verification.isEmailValid(email)) {
            if (verification.getIsEmailEmpty()) {
                onFinishedListener.onVerificationError(VerificationType.EMPTY_EMAIL)
            } else {
                onFinishedListener.onVerificationError(VerificationType.INVALID_EMAIL)
            }
            isValid = false
        } else {
            onFinishedListener.onValidEmail()
        }
        if (!verification.isPasswordValid(password)) {
            if (verification.getIsPasswordEmpty()) {
                onFinishedListener.onVerificationError(VerificationType.EMPTY_PASSWORD)
                isValid = false
            }
            else {
                onFinishedListener.onValidPassword()
            }
        } else {
            onFinishedListener.onValidPassword()
        }
        return isValid
    }

}