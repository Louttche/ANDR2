package com.example.stalkr

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.example.stalkr.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.rpc.context.AttributeContext
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // when app is initially opened the Map Fragment should be visible
        changeFragment(MapFragment())
    }

    /* Auth */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.action_logout == item.itemId) {
            Firebase.auth.signOut()
            signOut()
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun signOut() {
        // Set auth user as 'inactive' in DB
        AuthUserObject.isActive = false
        val users = AuthActivity.db.collection("users")

        users.whereEqualTo("uid", AuthActivity.userDbData!!.uid)
            .get()
            .addOnSuccessListener { documents ->
                val userActive = hashMapOf("isActive" to false)
                users.document(documents.first().id).set(userActive, SetOptions.merge())
            }

        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    // function to change the fragment which is used to reduce the lines of code
    private fun changeFragment(fragmentToChange: Fragment): Unit {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, fragmentToChange)
            addToBackStack(null)
            commit()
        }
    }
}