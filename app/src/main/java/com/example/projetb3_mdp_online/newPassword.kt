package com.example.projetb3_mdp_online

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var passwordInput: TextInputEditText
    private lateinit var siteInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var notesInput: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var biometricSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        siteInput = findViewById(R.id.siteInput)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        notesInput = findViewById(R.id.notesInput)
        saveButton = findViewById(R.id.saveButton)
        biometricSwitch = findViewById(R.id.biometricSwitch)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set up back button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up save button
        saveButton.setOnClickListener {
            savePassword()
        }
    }

    private fun savePassword() {
        val site = siteInput.text.toString()
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        val notes = notesInput.text.toString()
        val useBiometric = biometricSwitch.isChecked

        // Validate input
        if (site.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Here you would normally save the password to your database
        // For example:
        // val passwordItem = PasswordItem(site, username, password, notes, useBiometric)
        // passwordViewModel.addPassword(passwordItem)

        // Show success message
        Toast.makeText(this, "Password saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}