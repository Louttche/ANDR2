package com.example.stalkr.interfaces

import com.example.stalkr.fragments.login.LoginContract

interface MVP {
    interface BaseView {
        /**
         * Set presenter on view create
         */
        fun setPresenter()
    }

    interface BasePresenter {
        /**
         * Clean up view on view destroy during the lifecycle
         */
        fun onDestroy()
    }
}