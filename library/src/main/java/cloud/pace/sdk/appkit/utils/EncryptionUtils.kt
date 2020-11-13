package cloud.pace.sdk.appkit.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/*
* Note: this encryption method is not safe for encoding communication content, it should only be used for e.g. securely saving values to Preferences
*/
object EncryptionUtils {

    private const val KEY_STORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "PWAKIT_KEY"
    private val FIXED_IV = byteArrayOf(55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44)

    fun encrypt(data: String): String {
        val keyStore = KeyStore.getInstance(KEY_STORE)
        keyStore.load(null)

        // if key not created yet, do it now
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEY_STORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    PURPOSE_ENCRYPT or PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()
            )

            // this also stores the key
            keyGenerator.generateKey()
        }

        val key = keyStore.getKey(KEY_ALIAS, null)

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, FIXED_IV))
        val encodedBytes = cipher.doFinal(data.toByteArray())

        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val keyStore = KeyStore.getInstance(KEY_STORE)
        keyStore.load(null)
        val key = keyStore.getKey(KEY_ALIAS, null)

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, FIXED_IV))
        val decoded = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))

        return String(decoded)
    }
}
