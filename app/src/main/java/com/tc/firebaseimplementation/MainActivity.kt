package com.tc.firebaseimplementation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.RuntimeException

import com.tc.firebaseimplementation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var analytics: FirebaseAnalytics
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private val TAG = "MainActivity"
    private val RC_SIGN_IN = 9001

    //Firebase  Google


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END config_signin]


//        signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.your_web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            .build()

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

            // [START initialize_fblogin]
            // Initialize Facebook Login button
            callbackManager = CallbackManager.Factory.create()


            fbloginbutton.setReadPermissions("email", "public_profile")
            fbloginbutton.registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d(TAG, "facebook:onSuccess:$loginResult")
                        handleFacebookAccessToken(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "facebook:onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Log.d(TAG, "facebook:onError", error)
                    }
                },
            )
            // [END initialize_fblogin]

//            buttonLog.setOnClickListener {
//                analytics.logEvent(
//                    FirebaseAnalytics.Event.SELECT_CONTENT, bundleOf(
//                        FirebaseAnalytics.Param.ITEM_ID to "Abdullahi",
//                        FirebaseAnalytics.Param.ITEM_NAME to "Clicked a button!",
//                        FirebaseAnalytics.Param.CONTENT_TYPE to "button"
//                    )
//                )
//            }

            buttonCrash.setOnClickListener {
                throw RuntimeException("Peter did this!")
            }


            //facebook event

            googleSigninButton.setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)


            }
            buttonLogin.setOnClickListener {


                if (emailAndPasswordIsValid()) {
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


    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    // [END onactivityresult]


    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)

                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }


    // [END auth_with_google]


    // [START auth_with_facebook]
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }
    // [END auth_with_facebook]

    private fun updateUI(user: FirebaseUser?) {
        Toast.makeText(this, "$user", Toast.LENGTH_SHORT).show()

    }

    private fun emailAndPasswordIsValid(): Boolean {
        val email = binding.editTextTextEmailAddress.text.toString()
        val password = binding.editTextTextPassword.text.toString()
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
        if (currentUser != null) {
            updateSignedInUser(currentUser)
        }
    }

    private fun updateSignedInUser(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, user?.email ?: "USER IS ALREADY HERE!", Toast.LENGTH_SHORT).show()
        }
    }
}