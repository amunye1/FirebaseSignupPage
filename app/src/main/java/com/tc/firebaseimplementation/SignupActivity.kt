package com.tc.firebaseimplementation

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.google.firebase.ktx.Firebase
import com.tc.firebaseimplementation.databinding.SignupActivityBinding
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.Event

class SignupActivity : AppCompatActivity(){
    lateinit var binding :SignupActivityBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignupActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.apply {

            signupButton.setOnClickListener {
                val email = binding.signupEmail.text.toString()
                val password = binding.signupPassword.text.toString()
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }
            }
        }


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }



    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Signup was successful, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        } else {
            // Signup failed, display an error message
            Toast.makeText(
                baseContext,
                "Invalid email or password. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

