package com.example.projetb3_mdp_online

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.projetb3_mdp_online.api.ApiClient
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordAllActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var itemsContainer: LinearLayout
    private lateinit var progressIndicator: CircularProgressIndicator

    private var userId = -1 // Valeur par défaut, sera remplacée par la valeur passée
    private var passwordsList = listOf<PasswordData>() // Liste des mots de passe

    // Définir le lanceur d'activité pour ajouter un mot de passe
    private val addPasswordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Rafraîchir la liste des mots de passe car un nouveau mot de passe a été ajouté
            loadPasswords()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_all)

        // Récupérer l'ID utilisateur passé par PasswordGroupesActivity
        if (intent.hasExtra("USER_ID")) {
            userId = intent.getIntExtra("USER_ID", -1)
        }

        // Vérifier que l'ID utilisateur est valide
        if (userId == -1) {
            Toast.makeText(this, "Erreur: ID utilisateur non valide", Toast.LENGTH_SHORT).show()
            finish() // Terminer l'activité si aucun ID utilisateur valide n'est fourni
            return
        }

        // Initialisation des vues
        searchBar = findViewById(R.id.searchBar)
        itemsContainer = findViewById(R.id.itemsContainer)
        progressIndicator = findViewById(R.id.progressIndicator)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val addButtonContainer = findViewById<FrameLayout>(R.id.addButtonContainer)

        // Configuration du bouton de retour
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configuration du bouton d'ajout de mot de passe
        addButtonContainer.setOnClickListener {
            val intent = Intent(this, NewPasswordActivity::class.java)
            intent.putExtra("USER_ID", userId) // Passer l'ID utilisateur récupéré
            intent.putExtra("CATEGORY_ID", 2)
            addPasswordLauncher.launch(intent)
        }

        // Configuration de la recherche
        searchBar.addTextChangedListener { text ->
            filterPasswordsAndRefreshUI(text.toString())
        }

        // Charger les mots de passe avec l'ID utilisateur récupéré
        loadPasswords()
    }

    private fun loadPasswords() {
        // Afficher l'indicateur de progression
        progressIndicator.visibility = View.VISIBLE

        // Effectuer l'appel API avec l'ID utilisateur récupéré
        ApiClient.apiService.getPasswordsByUserId(userId).enqueue(object : Callback<PasswordsResponse> {
            override fun onResponse(call: Call<PasswordsResponse>, response: Response<PasswordsResponse>) {
                // Cacher l'indicateur de progression
                progressIndicator.visibility = View.GONE

                if (response.isSuccessful) {
                    val passwordsResponse = response.body()
                    if (passwordsResponse?.status == 200 && passwordsResponse.data != null) {
                        passwordsList = passwordsResponse.data.records
                        // Afficher les mots de passe
                        displayPasswords(passwordsList)
                    } else {
                        Toast.makeText(
                            this@PasswordAllActivity,
                            "Erreur: ${passwordsResponse?.message ?: "Données non disponibles"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@PasswordAllActivity,
                        "Erreur lors de la récupération des mots de passe",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("API_ERROR", "Code: ${response.code()}, Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PasswordsResponse>, t: Throwable) {
                // Cacher l'indicateur de progression
                progressIndicator.visibility = View.GONE

                Toast.makeText(
                    this@PasswordAllActivity,
                    "Erreur réseau: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API_ERROR", "Erreur: ${t.message}", t)
            }
        })
    }

    private fun displayPasswords(passwords: List<PasswordData>) {
        // Vider le conteneur
        itemsContainer.removeAllViews()

        if (passwords.isEmpty()) {
            // Afficher un message si aucun mot de passe n'est trouvé
            val noPasswordsView = TextView(this)
            noPasswordsView.text = "Aucun mot de passe trouvé"
            noPasswordsView.textSize = 16f
            noPasswordsView.setPadding(16, 16, 16, 16)
            itemsContainer.addView(noPasswordsView)
            return
        }

        // Créer une vue pour chaque mot de passe
        for (password in passwords) {
            // Créer la vue de l'élément
            val itemView = layoutInflater.inflate(R.layout.item_password, null)

            // Initialiser les vues de l'élément
            val tvInitials = itemView.findViewById<TextView>(R.id.tvInitials)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvUser = itemView.findViewById<TextView>(R.id.tvUser)

            // Définir les initiales (premières lettres du nom du site)
            val initials = if (password.site_name.isNotEmpty()) {
                password.site_name.take(2).uppercase()
            } else {
                "??"
            }

            // Définir les textes
            tvInitials.text = initials
            tvTitle.text = password.site_name
            tvUser.text = truncateUsername(password.username)

            // Configurer le clic sur l'élément
            itemView.setOnClickListener {
                navigateToPasswordDetails(password.password_id)
            }

            // Ajouter l'élément au conteneur
            itemsContainer.addView(itemView)

            // Ajouter un séparateur si ce n'est pas le dernier élément
            if (password != passwords.last()) {
                val separator = View(this)
                separator.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1 // 1dp de hauteur
                )
                separator.setBackgroundColor(getColor(R.color.separator_color))
                itemsContainer.addView(separator)
            }
        }
    }

    private fun truncateUsername(username: String): String {
        // Tronquer le nom d'utilisateur s'il est trop long
        return if (username.length > 12) {
            username.take(10) + "..."
        } else {
            username
        }
    }

    private fun filterPasswordsAndRefreshUI(query: String) {
        if (query.isEmpty()) {
            // Afficher tous les mots de passe
            displayPasswords(passwordsList)
            return
        }

        // Filtrer les mots de passe en fonction de la requête
        val filteredList = passwordsList.filter {
            it.site_name.contains(query, ignoreCase = true) ||
                    it.username.contains(query, ignoreCase = true)
        }

        // Afficher les mots de passe filtrés
        displayPasswords(filteredList)
    }

    private fun navigateToPasswordDetails(passwordId: Int) {
        val intent = Intent(this, PasswordDetailsActivity::class.java)
        intent.putExtra("PASSWORD_ID", passwordId.toLong())
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir la liste des mots de passe lorsque l'activité redevient visible
        loadPasswords()
    }
}