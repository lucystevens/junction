package uk.co.lucystevens.api.error

data class ErrorResponse(
    val title: String,
    val status: Int,
    val type: String,
    val details: List<String> = listOf()
)
