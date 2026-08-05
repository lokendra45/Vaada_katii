package com.gaatho.rent.database.converter

import androidx.room3.ColumnTypeConverter
import com.gaatho.rent.core.database.security.CryptoManager
import com.gaatho.rent.core.database.security.SecretString

class EncryptedStringConverter {
    @ColumnTypeConverter
    fun fromSecretString(secret: SecretString?): String? {
        return secret?.value?.let { CryptoManager.encrypt(it) }
    }

    @ColumnTypeConverter
    fun toSecretString(ciphertext: String?): SecretString? {
        return ciphertext?.let { SecretString(CryptoManager.decrypt(it)) }
    }
}

