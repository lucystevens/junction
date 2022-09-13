package uk.co.lucystevens.junction.db.models

import org.ktorm.entity.Entity
import java.time.Instant

interface AppConfig : Entity<AppConfig> {
    companion object : Entity.Factory<AppConfig>()

    var key: String
    var value: String
}
