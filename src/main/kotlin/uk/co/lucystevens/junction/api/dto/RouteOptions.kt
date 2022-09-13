package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

// TODO Remove ssl from route options,
// should be decided by domain settings
@Serializable
data class RouteOptions(
    val targets: List<RouteTarget>,
    val ssl: Boolean = false
)

