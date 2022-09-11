package uk.co.lucystevens.junction.api.ssl

import uk.co.lucystevens.junction.config.Config
import java.io.File
import java.security.KeyPair
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class KeyManager(private val config: Config, private val keyUtils: KeyUtils) {

    val keyPair by lazy { loadKeypair() }

    private fun loadKeypair(): KeyPair {
        val out = config.getDataDirectory()
            .resolve("keys")
            .createDirectories()
        val publicKeyFile = out.resolve("public")
        val privateKeyFile = out.resolve("private")
        return if(publicKeyFile.exists() && privateKeyFile.exists()){
            KeyPair(
                keyUtils.getPublicKey(publicKeyFile.readText()),
                keyUtils.getPrivateKey(privateKeyFile.readText())
            )
        }
        else {
            val keyPair = keyUtils.generateKeyPair()
            publicKeyFile.writeText(keyUtils.toString(keyPair.public))
            privateKeyFile.writeText(keyUtils.toString(keyPair.private))

            keyPair
        }


    }
}