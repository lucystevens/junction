package uk.co.lucystevens.junction.api.ssl

import org.shredzone.acme4j.util.KeyPairUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer


class KeyUtils {

    private val algorithm = "RSA"
    private val keySize = 2048

    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()

    fun getOrCreateKeyPair(keyPairFile: Path): KeyPair =
        if(keyPairFile.exists()) readKeyPair(keyPairFile)
        else createKeyPair(keyPairFile)

    fun createKeyPair(keyPairFile: Path): KeyPair =
        KeyPairUtils.createKeyPair(keySize).apply {
            writeKeyPair(keyPairFile, this)
        }

    fun readKeyPair(keyPairFile: Path): KeyPair =
        keyPairFile.reader().use {
            KeyPairUtils.readKeyPair(it)
        }

    fun writeKeyPair(keyPairFile: Path, keyPair: KeyPair) =
        keyPairFile.writer().use {
            KeyPairUtils.writeKeyPair(keyPair, it)
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