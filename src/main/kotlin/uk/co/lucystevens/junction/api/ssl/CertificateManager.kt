package uk.co.lucystevens.junction.api.ssl

import uk.co.lucystevens.junction.config.Config
import java.nio.file.Path
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import kotlin.io.path.inputStream
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

class CertificateManager(
    private val config: Config,
    private val keyManager: KeyManager) {

    // Can also be default
    private val keyStore = KeyStore.getInstance("jks").apply {
        load(null)
    }

    val sslContext by lazy { createSslContext() }

    fun addCertificate(alias: String, certChain: List<X509Certificate>){
        keyStore.setKeyEntry(
            alias,
            keyManager.keyPair.private,
            config.getCertificatePassword(),
            certChain.toTypedArray())
    }

    private fun loadCertificates() {
        config.getDataDirectory()
            .listDirectoryEntries()
            .filter { it.endsWith(".crt") }
            .forEach {
                addCertificate(
                    it.nameWithoutExtension,
                    readCert(it)
                )
            }
    }

    private fun readCert(filePath: Path): List<X509Certificate> {
        val factory = CertificateFactory.getInstance("X509")
        return filePath.inputStream().use { stream ->
            factory.generateCertificates(stream)
                .map { it as X509Certificate }
        }
    }

    private fun createSslContext(): SSLContext{
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, config.getCertificatePassword())
            loadCertificates()

            return SSLContext.getInstance("TLS").apply {
                init(kmf.keyManagers, null, null)
            }
        }
}