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
        val keyPairFile = config.getDataDirectory()
            .resolve("keypair.pem")
        return keyUtils.getOrCreateKeyPair(keyPairFile)
    }
}