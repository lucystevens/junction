package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class DomainRequestDto(
    val name: String,
    val redirectToHttps: Boolean?,
    val enableSsl: Boolean?
)
