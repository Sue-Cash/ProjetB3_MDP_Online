package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetb3_mdp_online.api.ApiClient
import com.example.projetb3_mdp_online.PasswordData
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordDetailsActivity : AppCompatActivity() {

    private lateinit var siteValue: TextView
    private lateinit var usernameValue: TextView
    private lateinit var passwordValue: TextView
    private lateinit var notesValue: TextView
    private lateinit var biometricSwitch: SwitchMaterial
    private lateinit var togglePasswordVisibility: ImageButton
    private lateinit var progressIndicator: CircularProgressIndicator

    private var passwordId: Long = 0
    private var actualPassword: String = "" // Pour stocker le mot de passe actuel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupère l'ID du mot de passe depuis l'intent (pour une utilisation future)
        if (intent.hasExtra("PASSWORD_ID")) {
            passwordId = intent.getLongExtra("PASSWORD_ID", 0)
        }

        // Initialise les vues
        siteValue = findViewById(R.id.siteValue)
        usernameValue = findViewById(R.id.usernameValue)
        passwordValue = findViewById(R.id.passwordValue)
        notesValue = findViewById(R.id.notesValue)
        biometricSwitch = findViewById(R.id.biometricSwitch)
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility)
        progressIndicator = findViewById(R.id.progressIndicator)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val editButton = findViewById<ImageButton>(R.id.editButton)

        // Charge les données du mot de passe
        loadPasswordData()

        // Configure le bouton de retour
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configure le bouton d'édition
        editButton.setOnClickListener {
            val intent = Intent(this, EditPasswordActivity::class.java)
            intent.putExtra("PASSWORD_ID", passwordId)
            intent.putExtra("SITE_NAME", siteValue.text.toString())
            intent.putExtra("USERNAME", usernameValue.text.toString())
            intent.putExtra("PASSWORD", actualPassword)
            intent.putExtra("NOTES", notesValue.text.toString())
            intent.putExtra("BIOMETRIC", biometricSwitch.isChecked)
            startActivity(intent)
        }

        // Configure le bouton de visibilité du mot de passe
        var isPasswordVisible = false
        togglePasswordVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Affiche le mot de passe
                passwordValue.text = actualPassword
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_on)
            } else {
                // Masque le mot de passe
                passwordValue.text = "••••••••••••••"
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
        }
    }

    private fun loadPasswordData() {
        // Affiche l'indicateur de chargement
        progressIndicator.visibility = View.VISIBLE

        // Effectue l'appel API

        ApiClient.apiService.getPasswordById(passwordId.toInt()).enqueue(object : Callback<PasswordResponse> {
            override fun onResponse(call: Call<PasswordResponse>, response: Response<PasswordResponse>) {
                // Cache l'indicateur de chargement
                progressIndicator.visibility = View.GONE

                if (response.isSuccessful) {
                    val passwordResponse = response.body()
                    if (passwordResponse?.status == 200 && passwordResponse.data != null) {
                        displayPasswordData(passwordResponse.data)
                    } else {
                        Toast.makeText(
                            this@PasswordDetailsActivity,
                            "Erreur: ${passwordResponse?.message ?: "Données non disponibles"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@PasswordDetailsActivity,
                        "Erreur lors de la récupération du mot de passe",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("API_ERROR", "Code: ${response.code()}, Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PasswordResponse>, t: Throwable) {
                // Cache l'indicateur de chargement
                progressIndicator.visibility = View.GONE

                Toast.makeText(
                    this@PasswordDetailsActivity,
                    "Erreur réseau: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API_ERROR", "Erreur: ${t.message}", t)
            }
        })
    }

    private fun displayPasswordData(data: PasswordData) {
        // Affiche les données du mot de passe
        siteValue.text = data.site_name
        usernameValue.text = data.username

        // Stocke le mot de passe réel, mais affiche des points pour le masquer
        actualPassword = data.password
        passwordValue.text = "••••••••••••••"

        // Affiche les notes
        notesValue.text = data.notes.ifEmpty { "Aucune note" }

        // Configure le switch de protection biométrique
        biometricSwitch.isChecked = data.biometric_protection == 1
    }

    override fun onResume() {
        super.onResume()
        // Recharge les données si on revient de l'écran d'édition
        loadPasswordData()
    }
}