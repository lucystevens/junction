package uk.co.lucystevens.junction.services

import io.undertow.util.BadRequestException
import org.shredzone.acme4j.Certificate
import org.shredzone.acme4j.util.CSRBuilder
import uk.co.lucystevens.junction.api.dto.DomainRequestDto
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.db.dao.DomainDao
import uk.co.lucystevens.junction.db.models.DomainData
import uk.co.lucystevens.junction.services.acme.ChallengeService
import uk.co.lucystevens.junction.utils.writeToString
import java.security.KeyPair

class DomainService(
    private val domainDao: DomainDao,
    private val challengeService: ChallengeService,
    private val certificateManager: CertificateManager
) {

    private val cache = domainDao.getDomains()
        .associateBy { it.name }
        .toMutableMap()

    fun updateDomainSettings(domainRequest: DomainRequestDto) {
        var domain = getDomain(domainRequest.name)

        // If not exists then create
        val requestCert = if(domain == null){
            domain = DomainData().apply {
                name = domainRequest.name
                ssl = domainRequest.enableSsl ?: false
                redirectToHttps = domainRequest.redirectToHttps ?: false
            }

            insertDomain(domain)
            domain.ssl
        }
        else {
            // Check if the ssl status has changed
            val sslChanged = domainRequest.let {
                it.enableSsl != null && it.enableSsl != domain.ssl
            }
            if(sslChanged){
                domain.ssl = domainRequest.enableSsl!!

                // Remove SSL certs if ssl disabled
                if(!domain.ssl){
                    domain.csr = null
                    domain.certificate = null
                    domain.expiry = null
                }
            }
            domainRequest.redirectToHttps?.let {
                domain.redirectToHttps = it
            }
            sslChanged
        }

        // Request certs async
        if(requestCert){
            requestCertificate(domain)
        }
    }

    private fun insertDomain(domain: DomainData){
        saveDomain(domain, domainDao::insertDomain)
    }

    private fun updateDomain(domain: DomainData){
        saveDomain(domain, domainDao::updateDomain)
    }

    private fun saveDomain(domain: DomainData, saveFn: (DomainData) -> Unit){
        // do validation
        if(!domain.ssl && domain.redirectToHttps){
            throw BadRequestException("Cannot redirect to https when ssl disabled.")
        }
        cache[domain.name] = domain
        saveFn(domain)
    }

    // Request certificates and update entity async
    private fun requestCertificate(domain: DomainData){
        Thread {
            val certificateResult = challengeService.requestCert(domain.name)
            addCertsToDomain(
                domain,
                certificateResult.csr,
                certificateResult.cert,
                certificateResult.keyPair
            )
            certificateManager.addCertificate(
                domain.name,
                certificateResult.keyPair,
                certificateResult.cert.certificateChain
            )
        }.start()
    }

    fun addCertsToDomain(domain: DomainData, csr: CSRBuilder, cert: Certificate, keyPair: KeyPair){
        // TODO encrypt files
        domain.apply {
            this.csr = writeToString { csr.write(it) }
            this.certificate = writeToString { cert.writeCertificate(it) }
            this.keyPair = keyPair.writeToString()
            this.expiry = cert.certificate.notAfter.toInstant()
        }

        updateDomain(domain)
    }

    fun removeDomain(domain: String) =
        domainDao.getDomain(domain)?.let {
            cache.remove(domain)
            domainDao.removeDomain(it)
        }

    fun getDomains() = cache.values

    fun getDomain(domain: String): DomainData? = cache[domain]
}