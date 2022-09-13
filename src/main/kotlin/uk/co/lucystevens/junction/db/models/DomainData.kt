package uk.co.lucystevens.junction.db.models

import org.ktorm.entity.Entity
import java.time.Instant

interface DomainData : Entity<DomainData> {
    companion object : Entity.Factory<DomainData>()

    var name: String
    var ssl: Boolean
    var redirectToHttps: Boolean
    var csr: String?
    var certificate: String?
    var keyPair: String?
    var expiry: Instant?
}
