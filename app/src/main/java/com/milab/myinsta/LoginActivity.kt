package com.milab.myinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Toast.makeText(this, "Test Toast", Toast.LENGTH_LONG).show()
        // To check if user is already logged in
        val auth = FirebaseAuth.getInstance()
        // If logged in already, continue session
        if(auth.currentUser != null){
            goPostsActivity()
        }

        btnLogin.setOnClickListener {
            //Login button disabled to prevent multiple post activities
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email or Password cannot be empty", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
                return@setOnClickListener
            }
            // Firebase authentication
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                // Login button enabled after result of authentication
                btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Successfully logged in!!", Toast.LENGTH_SHORT).show()
                    goPostsActivity()

                } else {
                    Log.e(TAG, "signInWithEmail failed", task.exception)
                    Toast.makeText(this, "Authentication failed!!", Toast.LENGTH_SHORT).show()

                }
            }
        }

    }
        private fun goPostsActivity() {
            Log.i(TAG, "goPostsActivity")
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
            // To stop app from returning to LoginActivity
            finish()
        }

}
