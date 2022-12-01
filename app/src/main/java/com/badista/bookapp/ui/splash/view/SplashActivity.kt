package com.badista.bookapp.ui.splash.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.badista.bookapp.R
import com.badista.bookapp.databinding.ActivitySplashBinding
import com.badista.bookapp.ui.dashboard.view.DashboardAdminActivity
import com.badista.bookapp.ui.dashboard.view.DashboardUserActivity
import com.badista.bookapp.ui.main.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

//    private lateinit var binding: ActivitySplashBinding

    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var handler: Handler

    companion object{
        const val TAG = "SPLASH_SCREEN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        handler = Handler()
        handler.postDelayed({
            checkUser()
        }, 2000)
    }

    private fun checkUser(){
        val firebaseUser = firebaseAuth.currentUser
        Log.d(TAG, "checkUser: firebaseUser : ${firebaseAuth.currentUser}")
        if (firebaseUser == null){
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            Log.d(TAG, "checkUser: Direct to Main Activity")
            finish()
        }
        else {
            val firebaseUser = firebaseAuth.currentUser!!
            Log.d(TAG, "checkUser: firebaseUser not null : $firebaseUser")
            
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.child("userType").value
                        if (userType == "user"){
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            Log.d(TAG, "onDataChange: Direct to dashboard user")
                            finish()
                        }
                        else if (userType == "admin"){
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            Log.d(TAG, "onDataChange: Direct to dashboard admin")
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: $error")
                    }

                })
        }
    }
}