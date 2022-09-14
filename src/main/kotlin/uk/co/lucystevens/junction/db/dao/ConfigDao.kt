package uk.co.lucystevens.junction.db.dao

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.update
import uk.co.lucystevens.junction.db.models.AppConfig
import uk.co.lucystevens.junction.db.models.config

class ConfigDao(
    private val database: Database
) {

    fun getConfig(key: String): AppConfig? =
        database.config.find { it.key eq key }

    fun getValue(key: String): String? =
        getConfig(key)?.value

    fun setValue(key: String, value: String) {
        val config = getConfig(key)
        if(config == null){
            AppConfig().apply {
                this.key = key
                this.value = value
                database.config.add(this)
            }
        } else database.config.update(config)
    }
}