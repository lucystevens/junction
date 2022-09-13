package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Certificate
import org.shredzone.acme4j.util.CSRBuilder
import java.security.KeyPair

data class CertificateResult(
    val host: String,
    val csr: CSRBuilder,
    val cert: Certificate,
    val keyPair: KeyPair
    )
