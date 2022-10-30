package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    val route: RoutePath,
    var targets: List<RouteTarget>,
)
