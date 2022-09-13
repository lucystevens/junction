package uk.co.lucystevens.junction.db.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.json
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import uk.co.lucystevens.junction.api.dto.RouteOptions

const val schema = "junction"

val columnOptions = mutableMapOf<Column<*>, MutableList<String>>()

object Routes : Table<Route>(tableName = "routes", schema = schema) {
    val host = varchar("host").primaryKey().bindTo { it.host }
    val path = varchar("path").primaryKey().bindTo { it.path }
    val options = json<RouteOptions>("options").notNull().bindTo { it.options }
}

object AppConfigs : Table<AppConfig>(tableName = "config", schema = schema) {
    val key = varchar("key").primaryKey().bindTo { it.key }
    val value = varchar("value").notNull().bindTo { it.value }
}

object Domains : Table<DomainData>(tableName = "domains", schema = schema) {
    val name = varchar("name").primaryKey().bindTo { it.name }
    val ssl = boolean("ssl").bindTo { it.ssl }
    val redirectToHttps = boolean("redirectToHttps").bindTo { it.redirectToHttps }
    val csr = varchar("csr").bindTo { it.csr }
    val certificate = varchar("certificate").bindTo { it.certificate }
    val keyPair = varchar("keypair").bindTo { it.keyPair }
    val expiry = timestamp("expiry").bindTo { it.expiry }
}

val Database.routes get() = this.sequenceOf(Routes)
val Database.config get() = this.sequenceOf(AppConfigs)
val Database.domains get() = this.sequenceOf(Domains)


fun <T: Any> Column<T>.addOption(option: String): Column<T> = apply {
    columnOptions.getOrPut(this) { mutableListOf() }
        .add(option)
}

fun <T: Any> Column<T>.notNull() = addOption("NOT NULL")
fun <T: Any> Column<T>.unique() = addOption("UNIQUE")
fun <T: Any> Column<T>.withDefault(defaultVal: T) =
    addOption("DEFAULT $defaultVal")
fun Column<String>.withDefault(defaultVal: String) =
    addOption("DEFAULT '$defaultVal'")

fun <T : Entity<T>> Database.createIfNotExists(table: Table<T>){
    useTransaction {
        useConnection {
            it.prepareStatement("CREATE SCHEMA IF NOT EXISTS ${table.schema};")
                .executeUpdate()

            it.prepareStatement(table.generateSql())
                .executeUpdate()
        }
    }
}

fun <T : Entity<T>> Table<T>.generateSql() = """
        CREATE TABLE IF NOT EXISTS $schema.$tableName (
            ${columns.joinToString(",\n") { it.generateSql() }},
            CONSTRAINT pk_$tableName PRIMARY KEY (${primaryKeys.joinToString(",") { it.name }})
        )
    """.trimIndent()

fun <T: Any> Column<T>.generateSql(): String {
    var options = columnOptions[this]?.joinToString(" ") ?: ""
    if(table.primaryKeys.contains(this) && !options.contains("NOT NULL")){
        options = "NOT NULL $options"
    }
    return "$name ${sqlType.typeName} $options"
}