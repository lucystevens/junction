package uk.co.lucystevens.api.error

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.javalin.http.Context
import io.javalin.http.ExceptionHandler

class ErrorHandler : ExceptionHandler<Exception> {

    private val defaultErrorResponse = ErrorResponse(
        title = "Something went wrong",
        500,
        type = "SERVER_ERROR"
    )

    override fun handle(e: Exception, ctx: Context) {
        createErrorResponse(e).apply {
            ctx.status(status)
            ctx.json(this)
        }
    }

    private fun createErrorResponse(e: Exception): ErrorResponse {
        return when(e){
            is MissingKotlinParameterException -> ErrorResponse(
                title = "Missing required field: '${e.parameter.name}'",
                status = 400,
                type = "MISSING_FIELD",
            )
            else -> defaultErrorResponse
        }
    }
}