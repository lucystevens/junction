package uk.co.lucystevens.junction.config

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

class Config {

    private fun getNullableConfig(key: String): String? =
        getConfig(key, "").ifEmpty { null }

    private fun getConfig(key: String, defaultValue: String? = null): String =
        System.getenv(key)
            ?: defaultValue
            ?: throw IllegalStateException("Missing value for non-optional property $key")

    // Service config
    fun getServiceName(): String =
        getConfig("PROJECT_NAME", "junction")

    fun getServiceVersion(): String =
        getConfig("PROJECT_VERSION", "unknown")

    // Server config
    fun getServerHost(): String =
        getConfig("SERVER_HOST", "localhost")

    fun getHttpPort(): Int =
        getConfig("HTTP_PORT", "80").toInt()

    fun getHttpsPort(): Int =
        getConfig("HTTPS_PORT", "443").toInt()

    fun getApiPort(): Int =
        getConfig("API_PORT", "8000").toInt()

    // SSL
    fun getCertificatePassword(): CharArray =
        getConfig("CERT_PASSWORD").toCharArray()

    fun getLetsEncryptUrl(): String =
        getConfig("LETSENCRYPT_URL")

    fun getEmailAddress(): String =
        getConfig("EMAIL_ADDRESS")

    fun getDataDirectory(): Path =
        Paths.get(getConfig("DATA_DIRECTORY"))
            .createDirectories()

    fun getAdminToken(): String =
        getConfig("ADMIN_TOKEN")
}