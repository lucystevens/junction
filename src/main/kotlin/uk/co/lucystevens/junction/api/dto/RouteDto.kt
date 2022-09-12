package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    val route: Route,
    val options: RouteOptions
)
