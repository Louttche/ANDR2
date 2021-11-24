package com.example.stalkr.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.stalkr.R
import com.example.stalkr.interfaces.AuthFragmentCallback
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    private lateinit var authFragmentCallback: AuthFragmentCallback
    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText
    private lateinit var textViewLoginErrorMsg: TextView
    private lateinit var buttonLogin: Button
    private lateinit var buttonShowRegistration: Button
    private var isValid = true
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AuthFragmentCallback) {
            authFragmentCallback = context
        } else {
            throw RuntimeException(context.toString().plus(" must implement FragmentCallback"))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        buttonLogin = view.findViewById(R.id.buttonLogin)
        buttonShowRegistration = view.findViewById(R.id.buttonShowRegistration)
        textViewLoginErrorMsg = view.findViewById(R.id.textViewLoginErrorMsg)
        textInputEditTextEmail = view.findViewById(R.id.textInputEmail)
        textInputEditTextPassword = view.findViewById(R.id.textInputPassword)
        textInputLayoutEmail = view.findViewById(R.id.textFieldEmail)
        textInputLayoutPassword = view.findViewById(R.id.textFieldPassword)

        buttonShowRegistration.setOnClickListener {
            authFragmentCallback.onButtonClickShowRegistration()
        }

        buttonLogin.setOnClickListener {
            // Verify if email and password is valid
            verifyEmail()
            verifyPassword()

            // Login
            if (isValid) {
                loginWithEmailAndPassword(
                    textInputEditTextEmail.text.toString().trim(),
                    textInputEditTextPassword.text.toString()
                )
            } else {
                isValid = true
            }
        }
        return view
    }

    private fun loginWithEmailAndPassword(email: String, password: String) {
        val firebaseAuth = Firebase.auth

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                authFragmentCallback.onAuthenticationComplete()
            } else {
                textViewLoginErrorMsg.text = task.exception?.message
            }
        }
    }

    private fun verifyEmail() {
        // Check if email is empty or invalid
        if (textInputEditTextEmail.text.toString().isEmpty()) {
            isValid = false
            textInputLayoutEmail.error = resources.getString(R.string.email_error)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(textInputEditTextEmail.text.toString().trim())
                .matches()
        ) {
            isValid = false
            textInputLayoutEmail.error = resources.getString(R.string.error_invalid_email)
        } else {
            textInputLayoutEmail.isErrorEnabled = false
        }
    }

    private fun verifyPassword() {
        // Check if password is empty or invalid
        if (textInputEditTextPassword.text.toString().isEmpty()) {
            isValid = false
            textInputLayoutPassword.error = resources.getString(R.string.password_error)
        } else {
            textInputLayoutPassword.isErrorEnabled = false
        }
    }

}