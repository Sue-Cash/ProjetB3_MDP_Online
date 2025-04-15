package com.example.projetb3_mdp_online

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
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
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var passwordInput: TextInputEditText
    private lateinit var siteInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var notesInput: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var biometricSwitch: SwitchMaterial
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var passwordSecurityManager: PasswordSecurityManager

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

        // Initialiser le gestionnaire de sécurité des mots de passe
        passwordSecurityManager = PasswordSecurityManager(this)

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

        try {
            // Chiffrer le mot de passe avant l'envoi
            // La clé de chiffrement est associée au site et à l'utilisateur
            val siteKey = "$site:$username"
            val encryptedPassword = passwordSecurityManager.encryptPassword(password, siteKey)

            // Stocker la relation site:utilisateur -> IVs dans les préférences locales
            // (Cette opération est faite automatiquement dans passwordSecurityManager)

            // Crée la requête d'ajout avec le mot de passe chiffré
            val createRequest = PasswordCreateRequest(
                user_id = userId,
                category_id = categoryId,
                site_name = site,
                username = username,
                password = encryptedPassword,  // Le mot de passe chiffré
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
        } catch (e: Exception) {
            progressIndicator.visibility = View.GONE
            Toast.makeText(
                this@NewPasswordActivity,
                "Erreur de chiffrement: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("ENCRYPTION_ERROR", "Erreur: ${e.message}", e)
        }
    }
}

// Gestionnaire de sécurité des mots de passe
class PasswordSecurityManager(private val context: AppCompatActivity) {
    private val KEY_ALIAS = "password_encryption_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val GCM_TAG_LENGTH = 128
    private val SHARED_PREFS_NAME = "password_security_prefs"
    private val IV_MAP_PREFIX = "iv_map_"

    init {
        if (!doesKeyExist()) {
            generateKey()
        }
    }

    private fun doesKeyExist(): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.containsAlias(KEY_ALIAS)
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    // Chiffre un mot de passe et sauvegarde l'IV associé
    fun encryptPassword(plainPassword: String, siteKey: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainPassword.toByteArray(Charsets.UTF_8))

        // Sauvegarder l'IV pour ce site et utilisateur
        saveIvForSiteKey(siteKey, iv)

        // Format spécial pour stocker le mot de passe chiffré
        // Ce format sera reconnu lors du déchiffrement
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
    }

    // Déchiffre un mot de passe avec l'IV sauvegardé
    fun decryptPassword(encryptedPassword: String, siteKey: String): String {
        val iv = getIvForSiteKey(siteKey) ?: throw Exception("IV not found for this site and username")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        val encryptedBytes = android.util.Base64.decode(encryptedPassword, android.util.Base64.DEFAULT)
        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted, Charsets.UTF_8)
    }

    // Sauvegarde l'IV pour un site et un utilisateur spécifiques
    private fun saveIvForSiteKey(siteKey: String, iv: ByteArray) {
        val ivBase64 = android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        prefs.edit().putString(IV_MAP_PREFIX + siteKey, ivBase64).apply()
    }

    // Récupère l'IV pour un site et un utilisateur spécifiques
    private fun getIvForSiteKey(siteKey: String): ByteArray? {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        val ivBase64 = prefs.getString(IV_MAP_PREFIX + siteKey, null) ?: return null
        return android.util.Base64.decode(ivBase64, android.util.Base64.DEFAULT)
    }
}