package com.argonlabs.cardsx.utils

import android.util.Base64
import android.util.Log
import com.scottyab.aescrypt.AESCrypt
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.spec.SecretKeySpec

class Locker {
    constructor() {}

    var sessionKey: String? = null

    constructor(sessionKey: String?) {
        this.sessionKey = sessionKey
    }

    fun encryptDataForUpload(data: String): String {
        val test = encryptData(data, sessionKey)
        return Base64.encodeToString(test, Base64.NO_WRAP)
    }

    fun decryptDataForShow(data: String?): String {
        val test = decryptData(Base64.decode(data, Base64.NO_WRAP), sessionKey)
        return try {
            String(test!!, Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            "empty"
        }
    }

    companion object {
        @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
        private fun generateKey(password: String?): SecretKeySpec {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = password!!.toByteArray(charset("UTF-8"))
            digest.update(bytes, 0, bytes.size)
            val key = digest.digest()
            return SecretKeySpec(key, "AES")
        }

        @JvmStatic
        fun encryptData(msg: String, pass: String?): ByteArray? {
            return try {
                val keyBytes2 = byteArrayOf(0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c,
                        0x0d, 0x0e, 0x0f, 0x0d, 0x0e, 0x0f, 0x0d, 0x0e, 0x0f)
                AESCrypt.encrypt(generateKey(pass), keyBytes2, msg.toByteArray())
            } catch (e: GeneralSecurityException) {
                //handle error
                // return e.getMessage();
                Log.e("TAGG", e.toString() + "")
                null
            } catch (e: UnsupportedEncodingException) {
                Log.e("TAGG", e.toString() + "")
                null
            }
        }

        fun decryptData(msg: ByteArray?, pass: String?): ByteArray? {
            return try {
                val keyBytes2 = byteArrayOf(0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c,
                        0x0d, 0x0e, 0x0f, 0x0d, 0x0e, 0x0f, 0x0d, 0x0e, 0x0f)
                AESCrypt.decrypt(generateKey(pass), keyBytes2, msg)
            } catch (e: GeneralSecurityException) {
                //handle error
                // return e.getMessage();
                Log.e("TAGG", e.toString() + "")
                null
            } catch (e: UnsupportedEncodingException) {
                Log.e("TAGG", e.toString() + "")
                null
            }
        }
    }
}