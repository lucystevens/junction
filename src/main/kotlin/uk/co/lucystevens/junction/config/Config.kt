package uk.co.lucystevens.junction.config

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

class Config(private val environment: Map<String, String> = System.getenv()) {

    private fun getNullableConfig(key: String): String? =
        getConfig(key, "").ifEmpty { null }

    private fun getConfig(key: String, defaultValue: String? = null): String =
        environment[key]
            ?: defaultValue
            ?: throw IllegalStateException("Missing value for non-optional property $key")

    // Service config
    fun getServiceName(): String =
        getConfig("PROJECT_NAME", "junction")

    fun getServiceVersion(): String =
        getConfig("PROJECT_VERSION", "unknown")

    // Server config
    fun getBindAddress(): String =
        getConfig("BIND_ADDRESS", "localhost")

    fun getHttpPort(): Int =
        getConfig("HTTP_PORT", "80").toInt()

    fun getHttpsPort(): Int =
        getConfig("HTTPS_PORT", "443").toInt()

    fun getApiPort(): Int =
        getConfig("API_PORT", "8000").toInt()

    // SSL
    fun getSecretKey(): CharArray =
        getConfig("SECRET_KEY").toCharArray()

    fun getRSAKeySize(): Int =
        getConfig("RSA_KEY_SIZE", "2048").toInt()

    fun getAcmeUrl(): String =
        getConfig("ACME_URL")

    fun getEmailAddress(): String =
        getConfig("EMAIL_ADDRESS")

    fun getDatastore(): Path =
        Paths.get(getConfig("DATASTORE"))
            .createDirectories()

    fun getAdminToken(): String? =
        getNullableConfig("ADMIN_TOKEN")
}