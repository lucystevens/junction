package uk.co.lucystevens.junction.api.ssl

import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


class KeyUtils {

    private val algorithm = "RSA"
    private val keySize = 2048

    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()

    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(algorithm)
        generator.initialize(keySize)
        return generator.generateKeyPair()
    }

    fun toString(key: Key): String =
        base64Encoder.encodeToString(key.encoded)

    fun getPrivateKey(keyString: String): RSAPrivateKey {
        val keyFactory = KeyFactory.getInstance(algorithm)
        val keySpec = PKCS8EncodedKeySpec(base64Decoder.decode(keyString))
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    fun getPublicKey(keyString: String): RSAPublicKey {
        val keyFactory = KeyFactory.getInstance(algorithm)
        val keySpec = X509EncodedKeySpec(base64Decoder.decode(keyString))
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }
}