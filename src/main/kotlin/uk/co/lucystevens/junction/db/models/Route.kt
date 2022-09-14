package uk.co.lucystevens.junction.db.models

import org.ktorm.entity.Entity
import uk.co.lucystevens.junction.api.dto.RouteTarget

interface Route : Entity<Route> {
    companion object : Entity.Factory<Route>()

    var host: String
    var path: String
    var targets: List<RouteTarget>
}
