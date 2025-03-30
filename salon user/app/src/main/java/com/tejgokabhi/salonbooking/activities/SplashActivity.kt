package com.tejgokabhi.salonbooking.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

       if(Firebase.auth.currentUser != null) {
           Handler(Looper.getMainLooper()).postDelayed({
               startActivity(Intent(this@SplashActivity, HomeMainActivity::class.java))
               finish()
           }, 2000)
       } else {
           Handler(Looper.getMainLooper()).postDelayed({
               startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
               finish()
           }, 2000)
       }
    }
}