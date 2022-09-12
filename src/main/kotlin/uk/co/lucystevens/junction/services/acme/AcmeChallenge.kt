package uk.co.lucystevens.junction.services.acme

data class AcmeChallenge(
    val domain: String,
    val token: String,
    val content: String,
    val callback: () -> Unit
)
