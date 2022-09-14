package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    val routePath: RoutePath,
    var targets: List<RouteTarget>,
)
