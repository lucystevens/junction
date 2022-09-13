package uk.co.lucystevens.junction.db.dao

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.map
import org.ktorm.entity.update
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.db.models.Route
import uk.co.lucystevens.junction.db.models.routes

class RouteDao(private val database: Database) {

    private fun Route.toDto() = RouteDto(
        RoutePath(host, path),
        options
    )

    fun getRoutes(): List<RouteDto> =
        database.routes.map { it.toDto() }

    private fun getRoute(routePath: RoutePath): Route? =
        database.routes.find {
                (it.host eq routePath.host) and
                (it.path eq routePath.path)
            }

    fun putRoute(routePath: RoutePath, options: RouteOptions) {
        var route = getRoute(routePath)
        if(route == null){
            route = Route().apply {
                this.host = routePath.host
                this.path = routePath.path
                this.options = options
            }
            database.routes.add(route)
        }
        else {
            route.options = options
            database.routes.update(route)
        }
    }

    fun removeRoute(routePath: RoutePath) {
        getRoute(routePath)?.delete()
    }
}