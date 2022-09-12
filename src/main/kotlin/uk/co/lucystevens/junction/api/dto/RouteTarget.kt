package uk.co.lucystevens.junction.api.dto

import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class RouteTarget(
    val scheme: String = "http",
    val host: String,
    val port: Int
){
    fun toURI() = URI("$scheme://$host:$port")
}
