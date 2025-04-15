import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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