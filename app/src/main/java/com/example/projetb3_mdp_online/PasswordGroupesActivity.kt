package com.example.projetb3_mdp_online

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class PasswordGroupesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This tells the activity to use activity_login.xml as its layout
        setContentView(R.layout.activity_password_groupes)


        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
