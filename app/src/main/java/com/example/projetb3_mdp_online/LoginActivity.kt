package com.example.projetb3_mdp_online

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This tells the activity to use activity_login.xml as its layout
        setContentView(R.layout.activity_login)
    }
}
