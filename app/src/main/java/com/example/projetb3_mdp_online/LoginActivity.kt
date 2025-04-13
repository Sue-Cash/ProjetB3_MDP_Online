package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projetb3_mdp_online.UserData
import com.example.projetb3_mdp_online.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val apiService = ApiClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
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

            if (email.isNotEmpty() && password.isNotEmpty()) {

                // Lancer la coroutine pour l'appel réseau
                // lifecycleScope est lié au cycle de vie de l'activité (s'annule automatiquement)
                lifecycleScope.launch(Dispatchers.IO) { // Dispatchers.IO pour les opérations réseau/disque
                    try {
                        val response = apiService.getUserByEmail(email)

                        // Revenir sur le thread principal pour traiter la réponse et MAJ l'UI
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val apiResponse = response.body()
                                if (apiResponse != null && apiResponse.status == 200 && apiResponse.data != null) {
                                    val userData = apiResponse.data
                                    Log.w("LoginActivity", "Vérification de body de la req : ${userData}")

                                    if (verifyPassword(password, userData.password_hash ?: "")) {
                                        // Connexion réussie !
                                        Toast.makeText(this@LoginActivity, "Connexion réussie pour ${email}!", Toast.LENGTH_LONG).show()
                                        // Naviguer vers l'activité principale (remplacez MainActivity::class.java)
                                         val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                         startActivity(intent)
                                         finish() // fermer l'activité Login
                                    } else {
                                        // Mot de passe incorrect
                                        Toast.makeText(this@LoginActivity, "Email ou mot de passe incorrect.", Toast.LENGTH_SHORT).show()
                                    }

                                } else {
                                    // Aucun utilisateur trouvé pour cet email
                                    Toast.makeText(this@LoginActivity, "Email ou mot de passe incorrect.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Erreur de l'API (ex: 404, 500)
                                Toast.makeText(this@LoginActivity, "Erreur serveur: ${response.code()}", Toast.LENGTH_SHORT).show()
                                Log.e("LoginActivity", "API Error Response Code: ${response.code()}, Message: ${response.message()}")
                            }
                        }
                    } catch (e: IOException) {
                        // Erreur réseau (pas de connexion, timeout...)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Erreur réseau. Vérifiez votre connexion.", Toast.LENGTH_SHORT).show()
                            Log.e("LoginActivity", "Network Error: ${e.message}", e)
                        }
                    } catch (e: Exception) {
                        // Autre erreur (ex: problème de parsing JSON, ...)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Une erreur est survenue.", Toast.LENGTH_SHORT).show()
                            Log.e("LoginActivity", "Generic Error: ${e.message}", e)
                        }
                    }
                }
            } else {
                // Champs vides (géré sur le thread principal directement)
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        // Si le mot de passe est stocké en clair (pour les tests)
        if (inputPassword == storedHash) return true

        // Si vous utilisez bcrypt ou une autre méthode de hachage,
        // vous devrez utiliser la bibliothèque appropriée
        // Exemple avec BCrypt:
        // return BCrypt.checkpw(inputPassword, storedHash)

        // Pour le moment, retourner false si les chaînes ne correspondent pas directement
        return false
    }

}