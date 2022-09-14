package uk.co.lucystevens.junction.db.dao

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.toList
import org.ktorm.entity.update
import uk.co.lucystevens.junction.db.models.DomainData
import uk.co.lucystevens.junction.db.models.domains
import java.nio.file.Path
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.io.path.inputStream

class DomainDao(private val database: Database) {

    fun getDomains(): List<DomainData> =
        database.domains.toList()

    fun getDomain(name: String): DomainData? =
        database.domains.find { it.name eq name }

    fun updateDomain(domain: DomainData) {
        database.domains.update(domain)
    }

    fun insertDomain(domain: DomainData) {
        database.domains.add(domain)
    }

    fun removeDomain(domain: DomainData) {
        domain.delete()
    }
}