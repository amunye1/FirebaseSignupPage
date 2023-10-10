package com.tc.firebaseimplementation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val text = "Welcome you are signed in "
        val duration = Toast.LENGTH_SHORT // or Toast.LENGTH_LONG for a longer duration
        Toast.makeText(applicationContext, text, duration)
    }
}