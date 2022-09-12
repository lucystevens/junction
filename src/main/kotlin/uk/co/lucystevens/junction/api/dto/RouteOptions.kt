package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RouteOptions(
    val targets: List<RouteTarget>,
    val ssl: Boolean = false
)

