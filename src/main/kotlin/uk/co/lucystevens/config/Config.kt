package uk.co.lucystevens.config

class Config {

    private fun getConfig(key: String, defaultValue: String? = null): String =
        System.getenv(key)
            ?: defaultValue
            ?: throw IllegalStateException("Missing value for non-optional property $key")

    // Service config
    fun getServiceName(): String =
        getConfig("PROJECT_NAME", "unknown")

    fun getServiceVersion(): String =
        getConfig("PROJECT_VERSION", "unknown")

    // Database config
    fun getDatabaseUrl(): String =
        getConfig("DATABASE_URL")

    fun getDatabaseUsername(): String =
        getConfig("DATABASE_USERNAME")

    fun getDatabasePassword(): String =
        getConfig("DATABASE_PASSWORD")

    fun getDatabaseDriver(): String =
        getConfig("DATABASE_DRIVER", "org.postgresql.Driver")

    // Server config
    fun getAppPort(): Int =
        getConfig("APP_PORT", "7000").toInt()

    fun getAdminToken(): String =
        getConfig("ADMIN_TOKEN")
}