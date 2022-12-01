package com.badista.bookapp.ui.main.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.badista.bookapp.R
import com.badista.bookapp.databinding.ActivityMainBinding
import com.badista.bookapp.ui.dashboard.view.DashboardUserActivity
import com.badista.bookapp.ui.login.view.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            Intent(this@MainActivity, LoginActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnSkip.setOnClickListener {
            Intent(this@MainActivity, DashboardUserActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}