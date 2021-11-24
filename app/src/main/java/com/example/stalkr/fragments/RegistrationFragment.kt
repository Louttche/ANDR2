package com.example.stalkr.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stalkr.R
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import com.example.stalkr.interfaces.AuthFragmentCallback
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class RegistrationFragment : Fragment() {
    private lateinit var authFragmentCallback: AuthFragmentCallback
    private lateinit var textViewErrorMsg: TextView
    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText
    private lateinit var textInputEditTextConfirmationPassword: TextInputEditText
    private lateinit var textInputEditTextName: TextInputEditText
    private lateinit var buttonShowLogin: Button
    private lateinit var buttonRegister: Button
    private var isValid = true
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputLayoutConfirmationPassword: TextInputLayout
    private lateinit var textInputLayoutName: TextInputLayout

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

        val view = inflater.inflate(R.layout.fragment_registration, container, false)
        textViewErrorMsg = view.findViewById(R.id.textViewErrorMessage)
        textInputEditTextEmail = view.findViewById(R.id.textInputEmailR)
        textInputEditTextPassword = view.findViewById(R.id.textInputPasswordR)
        textInputEditTextConfirmationPassword =
            view.findViewById(R.id.textInputConfirmationPasswordR)
        textInputEditTextName = view.findViewById(R.id.textInputNameR)
        buttonShowLogin = view.findViewById(R.id.buttonShowLogin)
        buttonRegister = view.findViewById(R.id.buttonRegisterR)
        textInputLayoutEmail = view.findViewById(R.id.textFieldEmailR)
        textInputLayoutPassword = view.findViewById(R.id.textFieldPasswordR)
        textInputLayoutConfirmationPassword = view.findViewById(R.id.textFieldConfirmationPasswordR)
        textInputLayoutName = view.findViewById(R.id.textFieldNameR)

        // Show login screen
        buttonShowLogin.setOnClickListener {
            authFragmentCallback.onButtonClickShowLogin()
        }

        // Create new account
        buttonRegister.setOnClickListener {
            // Verify name, email, password and password confirmation
            verifyEmail()
            verifyPassword()
            verifyName()
            verifyConfirmationPassword()

            // Check if all verification is valid
            if (isValid) {
                createUserByEmailAndPassword(
                    textInputEditTextEmail.text.toString().trim(),
                    textInputEditTextPassword.text.toString(),
                    textInputEditTextName.text.toString()
                )
            } else {
                isValid = true
            }
        }
        return view
    }

    private fun createUserByEmailAndPassword(email: String, password: String, name: String) {
        val firebaseAuth = Firebase.auth

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "uid" to task.result.user?.uid,
                        "name" to name,
                        "latitude" to 0,
                        "longitude" to 0
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .add(user)
                        .addOnSuccessListener {
                            authFragmentCallback.onAuthenticationComplete()
                        }
                        .addOnFailureListener { e -> textViewErrorMsg.text = e.message }
                } else {
                    textViewErrorMsg.text = task.exception?.message
                }
            }
    }

    private fun verifyName() {
        // Check if name is empty or invalid
        if (textInputEditTextName.text.toString().isEmpty()) {
            isValid = false
            textInputLayoutName.error = resources.getString(R.string.name_error)
        } else {
            textInputLayoutName.isErrorEnabled = false
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
        //Minimum eight characters, at least one letter and one number:
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        val pattern = Pattern.compile(passwordPattern)

        // Check if password is empty or invalid
        if (textInputEditTextPassword.text.toString().isEmpty()) {
            isValid = false
            textInputLayoutPassword.error = resources.getString(R.string.password_error)
        } else if (!(pattern.matcher(textInputEditTextPassword.text.toString()).matches())) {
            isValid = false
            textInputLayoutPassword.error = resources.getString(R.string.error_invalid_password)
        } else {
            textInputLayoutPassword.isErrorEnabled = false
        }
    }

    private fun verifyConfirmationPassword() {
        // Check if password is empty or invalid
        if (textInputEditTextConfirmationPassword.text.toString().isEmpty()) {
            isValid = false
            textInputLayoutConfirmationPassword.error = resources.getString(R.string.password_error)
        }
        // Confirmation password must match password
        else if (textInputEditTextPassword.text.toString() != textInputEditTextConfirmationPassword.text.toString()) {
            isValid = false
            textInputLayoutConfirmationPassword.error =
                resources.getString(R.string.confirmation_password_error)
        } else {
            textInputLayoutConfirmationPassword.isErrorEnabled = false
        }
    }
}