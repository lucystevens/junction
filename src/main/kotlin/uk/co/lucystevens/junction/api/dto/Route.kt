package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val host: String,
    val path: String = "/"
)
