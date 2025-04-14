package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PasswordGroupesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_groupes)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 1) Find the layout for "All"
        val cardAll = findViewById<LinearLayout>(R.id.passwordAll)

        // 2) Set a click listener to navigate to PasswordAllActivity
        cardAll.setOnClickListener {
            val intent = Intent(this, PasswordAllActivity::class.java)
            startActivity(intent)
        }
    }
}