package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.projetb3_mdp_online.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val apiService = ApiClient.apiService
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val nextButton = findViewById<Button>(R.id.nextButton)
        progressIndicator = findViewById(R.id.progressIndicator)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        nextButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Afficher l'indicateur de progression avant de commencer la requête
                progressIndicator.visibility = View.VISIBLE

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val response = apiService.getUserByEmail(email)
                        withContext(Dispatchers.Main) {
                            // Masquer l'indicateur de progression après la requête
                            progressIndicator.visibility = View.GONE

                            if (response.isSuccessful) {
                                val apiResponse = response.body()
                                if (apiResponse != null && apiResponse.status == 200 && apiResponse.data != null) {
                                    val userData = apiResponse.data

                                    // Stocker l'ID utilisateur dans SharedPreferences
                                    val sharedPref = getSharedPreferences("password_manager", MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putInt("user_id", userData.user_id)
                                        apply()
                                    }

                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val passwordMatches = BCrypt.checkpw(password, userData.password_hash)
                                        withContext(Dispatchers.Main) {
                                            if (passwordMatches) {
                                                val biometricManager = BiometricManager.from(this@LoginActivity)
                                                when (biometricManager.canAuthenticate()) {
                                                    BiometricManager.BIOMETRIC_SUCCESS -> {
                                                        // OK, on continue
                                                    }
                                                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                                                        Toast.makeText(this@LoginActivity, "Aucun matériel biométrique disponible", Toast.LENGTH_SHORT).show()
                                                        proceedToApp()
                                                        return@withContext
                                                    }
                                                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                                                        Toast.makeText(this@LoginActivity, "Matériel biométrique actuellement indisponible", Toast.LENGTH_SHORT).show()
                                                        proceedToApp()
                                                        return@withContext
                                                    }
                                                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                                        Toast.makeText(this@LoginActivity, "Aucune donnée biométrique enregistrée", Toast.LENGTH_SHORT).show()
                                                        proceedToApp()
                                                        return@withContext
                                                    }
                                                }

                                                val executor = ContextCompat.getMainExecutor(this@LoginActivity)

                                                val biometricPrompt = BiometricPrompt(
                                                    this@LoginActivity,
                                                    executor,
                                                    object : BiometricPrompt.AuthenticationCallback() {
                                                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                            super.onAuthenticationError(errorCode, errString)
                                                            Toast.makeText(this@LoginActivity, "Erreur : $errString", Toast.LENGTH_SHORT).show()
                                                        }

                                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                            super.onAuthenticationSucceeded(result)
                                                            Toast.makeText(this@LoginActivity, "Authentification réussie !", Toast.LENGTH_SHORT).show()
                                                            proceedToApp(userData.user_id) // Passer l'ID utilisateur à proceedToApp
                                                        }


                                                        override fun onAuthenticationFailed() {
                                                            super.onAuthenticationFailed()
                                                            Toast.makeText(this@LoginActivity, "Échec de l'authentification", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                )

                                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                                    .setTitle("Authentification biométrique")
                                                    .setSubtitle("Veuillez vous authentifier avec votre empreinte digitale")
                                                    .setNegativeButtonText("Annuler")
                                                    .build()

                                                biometricPrompt.authenticate(promptInfo)
                                                return@withContext
                                            } else {
                                                Toast.makeText(this@LoginActivity, "Email ou mot de passe incorrect.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@LoginActivity, "Email ou mot de passe incorrect.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@LoginActivity, "Erreur serveur !", Toast.LENGTH_SHORT).show()
                                Log.e("LoginActivity", "API Error Response Code: ${response.code()}, Message: ${response.message()}")
                            }
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            // Masquer l'indicateur en cas d'erreur
                            progressIndicator.visibility = View.GONE

                            Toast.makeText(this@LoginActivity, "Erreur réseau. Vérifiez votre connexion.", Toast.LENGTH_SHORT).show()
                            Log.e("LoginActivity", "Network Error: ${e.message}", e)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            // Masquer l'indicateur en cas d'erreur
                            progressIndicator.visibility = View.GONE

                            Toast.makeText(this@LoginActivity, "Une erreur est survenue.", Toast.LENGTH_SHORT).show()
                            Log.e("LoginActivity", "Generic Error: ${e.message}", e)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun proceedToApp(userId: Int = -1) {
        val intent = Intent(this@LoginActivity, PasswordAllActivity::class.java)
        if (userId != -1) {
            intent.putExtra("USER_ID", userId)
        }
        startActivity(intent)
        finish()
    }
}