package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PasswordGroupesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_groupes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Find the SecurityCard and DeletedCard
        val securityCard = findViewById<LinearLayout>(R.id.securityCard)
        val deletedCard = findViewById<LinearLayout>(R.id.deletedCard)

        // Set up click listener for Security card to navigate to Password Details
        securityCard.setOnClickListener {
            // Navigate to PasswordDetailsActivity
            val intent = Intent(this, PasswordDetailsActivity::class.java)
            // For testing, we'll pass a dummy password ID
            intent.putExtra("PASSWORD_ID", 1L)
            startActivity(intent)
        }

        // Set up click listener for Deleted card to navigate to New Password
        deletedCard.setOnClickListener {
            // Navigate to NewPasswordActivity
            val intent = Intent(this, NewPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}