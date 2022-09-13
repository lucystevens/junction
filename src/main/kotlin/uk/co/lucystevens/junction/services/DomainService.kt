package uk.co.lucystevens.junction.services

import org.shredzone.acme4j.Certificate
import org.shredzone.acme4j.util.CSRBuilder
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.db.dao.DomainDao
import uk.co.lucystevens.junction.services.acme.ChallengeService
import uk.co.lucystevens.junction.utils.writeToString
import java.security.KeyPair

class DomainService(
    private val domainDao: DomainDao,
    private val challengeService: ChallengeService,
    private val certificateManager: CertificateManager
) {

    // TODO handle more options
    // TODO Update JunctionRouteHandler
    fun updateDomain(domain: String) {
        val certificateResult = challengeService.requestCert(domain)
        addCertsToDomain(
            domain,
            certificateResult.csr,
            certificateResult.cert,
            certificateResult.keyPair)
        certificateManager.addCertificate(
            domain,
            certificateResult.keyPair,
            certificateResult.cert.certificateChain)
    }

    fun addCertsToDomain(host: String, csr: CSRBuilder, cert: Certificate, keyPair: KeyPair){
        val domain = domainDao.getDomain(host)
            ?: throw IllegalStateException("Domain $host not found")

        // TODO encrypt files
        domain.apply {
            this.csr = writeToString { csr.write(it) }
            this.certificate = writeToString { cert.writeCertificate(it) }
            this.keyPair = keyPair.writeToString()
            this.expiry = cert.certificate.notAfter.toInstant()
        }

        domainDao.updateDomain(domain)
    }

    // TODO Update JunctionRouteHandler
    fun removeDomain(domain: String) =
        domainDao.getDomain(domain)?.let {
            domainDao.removeDomain(it)
        }

    fun getDomains() = domainDao.getDomains()
}