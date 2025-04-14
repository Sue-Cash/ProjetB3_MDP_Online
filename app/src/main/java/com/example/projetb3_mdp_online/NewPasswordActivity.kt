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

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var passwordInput: TextInputEditText
    private lateinit var siteInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var notesInput: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var biometricSwitch: SwitchMaterial
    private lateinit var progressIndicator: CircularProgressIndicator

    // Valeurs par défaut si non fournies
    private var userId = 14
    private var categoryId = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupérer les ID depuis l'intent
        if (intent.hasExtra("USER_ID")) {
            userId = intent.getIntExtra("USER_ID", 14)
        }

        if (intent.hasExtra("CATEGORY_ID")) {
            categoryId = intent.getIntExtra("CATEGORY_ID", 2)
        }

        // Initialise les vues
        siteInput = findViewById(R.id.siteInput)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        notesInput = findViewById(R.id.notesInput)
        saveButton = findViewById(R.id.saveButton)
        biometricSwitch = findViewById(R.id.biometricSwitch)
        progressIndicator = findViewById(R.id.progressIndicator)

        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Configure le bouton retour
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configure le bouton de sauvegarde
        saveButton.setOnClickListener {
            savePassword()
        }
    }

    private fun savePassword() {
        val site = siteInput.text.toString()
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        val notes = notesInput.text.toString()
        val useBiometric = if (biometricSwitch.isChecked) 1 else 0

        // Valide les entrées
        if (site.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Affiche l'indicateur de progression
        progressIndicator.visibility = View.VISIBLE

        // Crée la requête d'ajout
        val createRequest = PasswordCreateRequest(
            user_id = userId,
            category_id = categoryId,
            site_name = site,
            username = username,
            password = password,
            biometric_protection = useBiometric,
            notes = notes
        )

        // Envoie la requête d'ajout
        ApiClient.apiService.addPassword(createRequest).enqueue(object : Callback<PasswordResponse> {
            override fun onResponse(call: Call<PasswordResponse>, response: Response<PasswordResponse>) {
                progressIndicator.visibility = View.GONE

                if (response.isSuccessful) {
                    val passwordResponse = response.body()
                    if (passwordResponse?.status == 201) {
                        Toast.makeText(
                            this@NewPasswordActivity,
                            "Mot de passe ajouté avec succès",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Définir un résultat pour indiquer le succès
                        setResult(RESULT_OK)

                        // Terminer l'activité pour revenir à la page précédente
                        finish()
                    } else {
                        Toast.makeText(
                            this@NewPasswordActivity,
                            "Erreur: ${passwordResponse?.message ?: "Échec de l'ajout"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@NewPasswordActivity,
                        "Erreur lors de l'ajout du mot de passe",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("API_ERROR", "Code: ${response.code()}, Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PasswordResponse>, t: Throwable) {
                progressIndicator.visibility = View.GONE

                Toast.makeText(
                    this@NewPasswordActivity,
                    "Erreur réseau: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API_ERROR", "Erreur: ${t.message}", t)
            }
        })
    }
}