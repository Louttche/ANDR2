package com.example.stalkr.fragments.login

import com.example.stalkr.enums.VerificationType
import com.example.stalkr.interfaces.MVP

interface LoginContract {
    interface View : MVP.BaseView {
        /**
         * Show error message for invalid password
         */
        fun showLoginError(msg: String)

        /**
         * Show error message for empty password
         */
        fun showEmptyPasswordError()

        /**
         * Show error message for invalid email
         */
        fun showInvalidEmailError()

        /**
         * Show error message for empty email
         */
        fun showEmptyEmailError()

        /**
         * Disable login input field error message
         */
        fun disableEmailLayoutError()

        /**
         * Disable password input field error message
         */
        fun disablePasswordLayoutError()

        /**
         * Show home screen if login was successful
         */
        fun showHomeScreen()
    }

    interface Presenter : MVP.BasePresenter {
        /**
         * Execute login when the login is clicked
         */
        fun onButtonLoginClicked(email: String, password: String)
    }
}