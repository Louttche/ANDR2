package com.example.stalkr

class Verification {
    private var isEmailEmpty = false
    private var isPasswordEmpty = false

    fun isEmailValid(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return if (email.isEmpty()) {
            isEmailEmpty = true
            false
        } else  {
            isEmailEmpty = false
            emailRegex.toRegex().matches(email)
        }
    }

    fun isPasswordValid(password: String): Boolean {
        val passwordRegex = ("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#\$%^&+=!])(?=\\S+\$).{4,}\$")
        return if (password.isEmpty()) {
            isPasswordEmpty = true
            false
        } else {
            isPasswordEmpty = false
            passwordRegex.toRegex().matches(password)
        }
    }

    fun getIsEmailEmpty(): Boolean {
        return isEmailEmpty
    }

    fun getIsPasswordEmpty(): Boolean {
        return isPasswordEmpty
    }
}