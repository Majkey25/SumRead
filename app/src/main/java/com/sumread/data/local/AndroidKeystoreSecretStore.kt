package com.sumread.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.sumread.util.AppConfig
import com.sumread.util.DispatchersProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class AndroidKeystoreSecretStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) : SecretStore {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override suspend fun save(alias: String, value: String) {
        withContext(dispatchersProvider.io) {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(alias))
            val encryptedBytes = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
            val combined = cipher.iv + encryptedBytes
            context.getSharedPreferences(AppConfig.secretsName, Context.MODE_PRIVATE)
                .edit()
                .putString(alias, Base64.encodeToString(combined, Base64.NO_WRAP))
                .apply()
        }
    }

    override suspend fun read(alias: String): String? {
        return withContext(dispatchersProvider.io) {
            val encoded = context.getSharedPreferences(AppConfig.secretsName, Context.MODE_PRIVATE)
                .getString(alias, null) ?: return@withContext null
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            val iv = combined.copyOfRange(fromIndex = 0, toIndex = ivSize)
            val payload = combined.copyOfRange(fromIndex = ivSize, toIndex = combined.size)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(alias),
                GCMParameterSpec(tagSizeBits, iv),
            )
            String(cipher.doFinal(payload), StandardCharsets.UTF_8)
        }
    }

    override suspend fun contains(alias: String): Boolean {
        return withContext(dispatchersProvider.io) {
            context.getSharedPreferences(AppConfig.secretsName, Context.MODE_PRIVATE).contains(alias)
        }
    }

    override suspend fun delete(alias: String) {
        withContext(dispatchersProvider.io) {
            context.getSharedPreferences(AppConfig.secretsName, Context.MODE_PRIVATE)
                .edit()
                .remove(alias)
                .apply()
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        }
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) {
            return existing
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    private companion object {
        const val transformation = "AES/GCM/NoPadding"
        const val ivSize = 12
        const val tagSizeBits = 128
    }
}
