package com.tc.firebaseimplementation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.RuntimeException

import com.tc.firebaseimplementation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var  analytics:FirebaseAnalytics
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics = Firebase.analytics
        auth = Firebase.auth
        binding = ActivityMainBinding.inflate(layoutInflater)

        analytics.logEvent(
            FirebaseAnalytics.Event.SELECT_CONTENT, bundleOf(
                FirebaseAnalytics.Param.ITEM_ID to "Sam",
                FirebaseAnalytics.Param.ITEM_NAME to "Activity started",
                FirebaseAnalytics.Param.CONTENT_TYPE to "lifecyle"
            )
        )

        binding.apply {
            buttonLog.setOnClickListener {
                analytics.logEvent(
                    FirebaseAnalytics.Event.SELECT_CONTENT, bundleOf(
                        FirebaseAnalytics.Param.ITEM_ID to "Abdullahi",
                        FirebaseAnalytics.Param.ITEM_NAME to "Clicked a button!",
                        FirebaseAnalytics.Param.CONTENT_TYPE to "button"
                    )
                )
            }

            buttonCrash.setOnClickListener {
                throw RuntimeException("Peter did this!")
            }

            buttonLogin.setOnClickListener {

                if(emailAndPasswordIsValid()){
                    auth.signInWithEmailAndPassword(
                        binding.editTextTextEmailAddress.text.toString(),
                        binding.editTextTextPassword.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MAIN_ACTIVITY", "signInWithEmail:success")
                            val user = auth.currentUser
                            updateSignedInUser(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MAIN_ACTIVITY", "signInWithEmail:failure", it.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateSignedInUser(null)
                        }
                    }
                }

            }
        }

        setContentView(binding.root)
    }

    private fun emailAndPasswordIsValid(): Boolean {
       val email= binding.editTextTextEmailAddress.text.toString()
        val password=binding.editTextTextPassword.text.toString()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val passwordPattern = "^(?=.*[0-9])(?=.*[!@#\$%^&*])(?=\\S+$).{8,}$"

        val isEmailValid = email.matches(emailPattern.toRegex())
        val isPasswordValid = password.matches(passwordPattern.toRegex())

        if (!isEmailValid) {
            binding.editTextTextEmailAddress.error = "Invalid email format"
        }
        if (!isPasswordValid) {
            binding.editTextTextPassword.error = "Invalid password format"
        }

        return isEmailValid && isPasswordValid

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser !=null){
            updateSignedInUser(currentUser)
        }
    }

    private fun updateSignedInUser(user: FirebaseUser?) {
        if (user != null) {
            // Signup was successful, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity to prevent going back to the signup screen
        }
    }
}