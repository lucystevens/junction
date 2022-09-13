package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoutePath(
    val host: String,
    val path: String = "/"
)
