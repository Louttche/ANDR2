package com.example.stalkr.fragments.login

import com.example.stalkr.enums.VerificationType
import com.example.stalkr.repositories.AuthRepo

class LoginPresenter(private var loginView: LoginContract.View?, private val authRepo: AuthRepo) : LoginContract.Presenter, AuthRepo.OnFinishedListener {

    override fun onButtonLoginClicked(email: String, password: String) {
        authRepo.loginWithEmailAndPassword(email, password, this)
    }

    override fun onDestroy() {
        loginView = null
    }

    override fun onLogin() {
        loginView?.showHomeScreen()
    }

    override fun onVerificationError(verificationType: VerificationType){
        if (verificationType == VerificationType.EMPTY_EMAIL) {
            loginView?.showEmptyEmailError()
        }
        else if (verificationType == VerificationType.INVALID_EMAIL) {
            loginView?.showInvalidEmailError()
        }
        if (verificationType == VerificationType.EMPTY_PASSWORD) {
            loginView?.showEmptyPasswordError()
        }
    }

    override fun onVerificationError(msg: String) {
        loginView?.showLoginError(msg)
    }

    override fun onValidEmail() {
        loginView?.disableEmailLayoutError()
    }

    override fun onValidPassword() {
       loginView?.disablePasswordLayoutError()
    }
}