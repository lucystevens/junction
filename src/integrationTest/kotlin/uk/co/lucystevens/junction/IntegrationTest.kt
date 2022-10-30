package uk.co.lucystevens.junction

import org.junit.jupiter.api.Test
import org.ktorm.database.Database

// TODO write a simple integration test for the happy path against a single container
// Use acceptance tests with mocked SSL/ACME for other paths
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