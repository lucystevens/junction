package uk.co.lucystevens.junction.test

import org.koin.dsl.module
import org.ktorm.database.Database
import uk.co.lucystevens.junction.config.Config

object TestModules {

    const val dbUrl = "jdbc:sqlite:file::memory:?cache=shared"

    // override a few classes for testing
    val test = module {
        single { Config(mapOf(
            "ACME_URL" to "acme://pebble",
            "SECRET_KEY" to "secret",
            "CERT_PASSWORD" to "password",
            "ADMIN_TOKEN" to "token",
            "EMAIL_ADDRESS" to "test@mail.com",
            "HTTP_PORT" to "5002",
            "HTTPS_PORT" to "8443",
            "BIND_ADDRESS" to "0.0.0.0"
        )) }

        single { Database.connect(
            url = dbUrl
        )}
    }
}