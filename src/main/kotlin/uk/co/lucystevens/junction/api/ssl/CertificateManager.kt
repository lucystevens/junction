package uk.co.lucystevens.junction.api.ssl

import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.db.models.DomainData
import uk.co.lucystevens.junction.utils.logger
import uk.co.lucystevens.junction.utils.readCert
import uk.co.lucystevens.junction.utils.readKeyPair
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509ExtendedKeyManager

class CertificateManager(
    private val config: Config
) {

    private val logger = logger<CertificateManager>()

    // Can also be default
    private val keyStore = KeyStore.getInstance("JKS").apply {
        load(null, config.getSecretKey())
    }

    private val keyManager by lazy {
        UpdatableKeyManager(createKeyManager())
    }

    fun addCertificate(alias: String, keyPair: KeyPair, certChain: List<X509Certificate>){
        keyStore.setKeyEntry(
            alias,
            keyPair.private,
            config.getSecretKey(),
            certChain.toTypedArray())
    }

    fun loadCertificates(domains: List<DomainData>) {
        domains.filter { it.ssl && it.certificate != null && it.keyPair != null }
            .forEach {
                addCertificate(
                    it.name,
                    it.keyPair!!.readKeyPair(),
                    it.certificate!!.readCert()
                )
            }
        updateKeyManager()
    }

    fun X509Certificate.logInfo(){
        logger.info("Reading certificate...")
        logger.info("Subject DN: ${subjectDN.name}")
        logger.info("Issuer DN: ${issuerDN.name}")
        logger.info("Serial No. $serialNumber")
        logger.info("Valid From: $notBefore")
        logger.info("Valid To: $notAfter")
    }

    // THIS WORKS!!!!!!
    fun updateKeyManager() {
        keyManager.keyManager = createKeyManager()
    }

    private fun createKeyManager(): X509ExtendedKeyManager =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).let {
            it.init(keyStore, config.getSecretKey())
            it.keyManagers[0] as X509ExtendedKeyManager
        }

    fun createSslContext(): SSLContext =
        SSLContext.getInstance("TLS").apply {
            init(arrayOf(keyManager), null, null)
        }

}