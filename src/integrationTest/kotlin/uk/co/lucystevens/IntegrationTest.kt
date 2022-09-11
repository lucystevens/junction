package uk.co.lucystevens

import org.junit.jupiter.api.Test
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

class IntegrationTest {

    @Test
    fun testDatabaseModels(){
        val db = Database.connect(
            url = "jdbc:postgresql://localhost:9001/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "password",
            dialect = PostgreSqlDialect()
        )

        // TODO add tests
    }
}