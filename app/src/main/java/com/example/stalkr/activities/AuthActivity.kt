package com.example.stalkr.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.stalkr.R
import com.example.stalkr.fragments.LoginFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val loginFragment = LoginFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerViewAuth, loginFragment, "LoginFragment")
        fragmentTransaction.commit()
    }
}