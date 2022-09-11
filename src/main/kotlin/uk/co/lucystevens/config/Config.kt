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

    // Server config
    fun getAppPort(): Int =
        getConfig("APP_PORT", "7000").toInt()

    fun getAdminToken(): String =
        getConfig("ADMIN_TOKEN")
}