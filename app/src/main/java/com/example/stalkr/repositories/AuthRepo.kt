package com.example.stalkr.repositories

import com.example.stalkr.enums.VerificationType
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

interface AuthRepo {
    interface OnFinishedListener {
        fun onLogin()
        fun onVerificationError(verificationType: VerificationType)
        fun onVerificationError(msg: String)
        fun onValidEmail()
        fun onValidPassword()

    }
    fun loginWithEmailAndPassword(email: String, password: String, onFinishedListener: OnFinishedListener)
}