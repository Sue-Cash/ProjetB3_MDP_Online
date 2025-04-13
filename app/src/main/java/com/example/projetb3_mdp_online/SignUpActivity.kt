package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import retrofit2.Response
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private val apiService = ApiClient.apiService
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_input)
        val nextButton = findViewById<Button>(R.id.nextButton)

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
            val confirmPassword = confirmPasswordInput.text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                    registerUser(email, hashedPassword)
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        // Vérifier si les champs sont vides
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return false
        }

        // Vérifier le format de l'email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Veuillez entrer une adresse email valide", Toast.LENGTH_SHORT).show()
            return false
        }

        // Vérifier la longueur du mot de passe
        if (password.length < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show()
            return false
        }

        // Vérifier si les mots de passe correspondent
        if (password != confirmPassword) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser(email: String, password: String) {
        // Créer l'objet de données pour l'inscription
        val registerRequest = RegisterRequest(email, password)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.registerUser(registerRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == 201) {
                            // Inscription réussie
                            Toast.makeText(this@SignUpActivity, "Inscription réussie! Vous pouvez maintenant vous connecter.", Toast.LENGTH_LONG).show()

                            // Rediriger vers la page de connexion
                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Erreur dans la réponse
                            Toast.makeText(this@SignUpActivity, apiResponse?.message ?: "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "API Response Error: ${apiResponse?.message}")
                        }
                    } else {
                        // Essayer de lire le message d'erreur si disponible
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "API Error Body: $errorBody")

                            // Si l'email existe déjà (code 409 généralement)
                            if (response.code() == 409) {
                                Toast.makeText(this@SignUpActivity, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SignUpActivity, "Erreur serveur: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@SignUpActivity, "Erreur serveur: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "Erreur réseau. Vérifiez votre connexion.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Network Error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "Une erreur est survenue: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Generic Error: ${e.message}", e)
                }
            }
        }
    }
}