package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PasswordGroupesActivity : AppCompatActivity() {

    private var userId: Int = -1 // Valeur par défaut

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_groupes)

        // Récupérer l'ID utilisateur passé par LoginActivity
        if (intent.hasExtra("USER_ID")) {
            userId = intent.getIntExtra("USER_ID", -1)
        }

        // Configurer les vues et les listeners
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurer le clic sur la carte "All"
        val allCard = findViewById<LinearLayout>(R.id.passwordAll)
        allCard.setOnClickListener {
            val intent = Intent(this, PasswordAllActivity::class.java)
            intent.putExtra("USER_ID", userId) // Passer l'ID utilisateur à PasswordAllActivity
            startActivity(intent)
        }

        // Configuration des autres cartes...
        val securityCard = findViewById<LinearLayout>(R.id.securityCard)
        securityCard.setOnClickListener {
            val intent = Intent(this, PasswordDetailsActivity::class.java)
            intent.putExtra("PASSWORD_ID", 2L)
            intent.putExtra("USER_ID", userId) // Passer également l'ID utilisateur ici si nécessaire
            startActivity(intent)
        }

        val deletedCard = findViewById<LinearLayout>(R.id.deletedCard)
        deletedCard.setOnClickListener {
            val intent = Intent(this, NewPasswordActivity::class.java)
            intent.putExtra("USER_ID", userId) // Passer l'ID utilisateur
            intent.putExtra("CATEGORY_ID", 2) // Catégorie par défaut
            startActivity(intent)
        }
    }
}