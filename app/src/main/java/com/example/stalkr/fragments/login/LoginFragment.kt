package com.example.stalkr.fragments.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.stalkr.R
import com.example.stalkr.interfaces.AuthFragmentCallback
import com.example.stalkr.repositories.AuthRepoImpl
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment(), LoginContract.View {
    private lateinit var authFragmentCallback: AuthFragmentCallback
    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText
    private lateinit var textViewLoginErrorMsg: TextView
    private lateinit var buttonLogin: Button
    private lateinit var buttonShowRegistration: Button
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private var presenter: LoginPresenter? = null

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

        // Initialized presenter
        setPresenter()

        // Initialized views
        buttonLogin = view.findViewById(R.id.buttonLogin)
        buttonShowRegistration = view.findViewById(R.id.buttonShowRegistration)
        textViewLoginErrorMsg = view.findViewById(R.id.textViewLoginErrorMsg)
        textInputEditTextEmail = view.findViewById(R.id.textInputEmail)
        textInputEditTextPassword = view.findViewById(R.id.textInputPassword)
        textInputLayoutEmail = view.findViewById(R.id.textFieldEmail)
        textInputLayoutPassword = view.findViewById(R.id.textFieldPassword)

        // Set Listeners
        buttonShowRegistration.setOnClickListener {
            authFragmentCallback.onButtonClickShowRegistration()
        }
        buttonLogin.setOnClickListener {
            val email = textInputEditTextEmail.text.toString()
            val password = textInputEditTextPassword.text.toString()
            presenter?.onButtonLoginClicked(email, password)
        }
        return view
    }

    override fun showLoginError(msg: String) {
        textViewLoginErrorMsg.text = msg
    }

    override fun showEmptyPasswordError() {
        textInputLayoutPassword.error = resources.getString(R.string.password_error)
    }

    override fun showInvalidEmailError() {
        textInputLayoutEmail.error = resources.getString(R.string.error_invalid_email)
    }

    override fun showEmptyEmailError() {
        textInputLayoutEmail.error = resources.getString(R.string.email_error)
    }

    override fun disableEmailLayoutError() {
        textInputLayoutEmail.isErrorEnabled = false
    }

    override fun disablePasswordLayoutError() {
        textInputLayoutPassword.isErrorEnabled = false
    }

    override fun showHomeScreen() {
        authFragmentCallback.onAuthenticationComplete()
    }

    override fun setPresenter() {
        presenter = LoginPresenter(this, AuthRepoImpl())
    }

}