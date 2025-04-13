package com.example.projetb3_mdp_online

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetb3_mdp_online.api.ApiClient
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPasswordActivity : AppCompatActivity() {

    private lateinit var passwordInput: TextInputEditText
    private lateinit var siteInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var notesInput: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var biometricSwitch: SwitchMaterial
    private lateinit var progressIndicator: CircularProgressIndicator

    private var passwordId: Int = 2 // Par défaut pour les tests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupère l'ID du mot de passe depuis l'intent
        if (intent.hasExtra("PASSWORD_ID")) {
            passwordId = intent.getLongExtra("PASSWORD_ID", 2).toInt()
        }

        // Récupère les informations du mot de passe depuis les extras
        val siteName = intent.getStringExtra("SITE_NAME") ?: ""
        val username = intent.getStringExtra("USERNAME") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""
        val notes = intent.getStringExtra("NOTES") ?: ""
        val biometric = intent.getBooleanExtra("BIOMETRIC", false)

        // Initialise les vues
        siteInput = findViewById(R.id.siteInput)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        notesInput = findViewById(R.id.notesInput)
        saveButton = findViewById(R.id.saveButton)
        biometricSwitch = findViewById(R.id.biometricSwitch)
        progressIndicator = findViewById(R.id.progressIndicator)

        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Remplir les champs avec les informations existantes
        siteInput.setText(siteName)
        usernameInput.setText(username)
        passwordInput.setText(password)
        notesInput.setText(notes)
        biometricSwitch.isChecked = biometric

        // Set up back button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up save button
        saveButton.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {
        val site = siteInput.text.toString()
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        val notes = notesInput.text.toString()
        val useBiometric = if (biometricSwitch.isChecked) 1 else 0

        // Validate input
        if (site.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress indicator
        progressIndicator.visibility = View.VISIBLE

        // Create update request
        val updateRequest = PasswordUpdateRequest(
            password_id = passwordId,
            site_name = site,
            username = username,
            password = password,
            biometric_protection = useBiometric,
            notes = notes
        )

        // Send update request
        ApiClient.apiService.updatePassword(updateRequest).enqueue(object : Callback<PasswordResponse> {
            override fun onResponse(call: Call<PasswordResponse>, response: Response<PasswordResponse>) {
                progressIndicator.visibility = View.GONE

                if (response.isSuccessful) {
                    val passwordResponse = response.body()
                    if (passwordResponse?.status == 200) {
                        Toast.makeText(
                            this@EditPasswordActivity,
                            "Mot de passe mis à jour avec succès",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditPasswordActivity,
                            "Erreur: ${passwordResponse?.message ?: "Échec de la mise à jour"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@EditPasswordActivity,
                        "Erreur lors de la mise à jour du mot de passe",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("API_ERROR", "Code: ${response.code()}, Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PasswordResponse>, t: Throwable) {
                progressIndicator.visibility = View.GONE

                Toast.makeText(
                    this@EditPasswordActivity,
                    "Erreur réseau: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API_ERROR", "Erreur: ${t.message}", t)
            }
        })
    }
}