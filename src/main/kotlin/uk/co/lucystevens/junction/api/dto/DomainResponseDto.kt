package uk.co.lucystevens.junction.api.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DomainResponseDto(
    val name: String,
    val redirectToHttps: Boolean,
    val ssl: SSLState,
    val expiry: Instant?
)
