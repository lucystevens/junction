package uk.co.lucystevens.junction

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.FileReader

val gson = GsonBuilder().setPrettyPrinting().create();

fun doRequest(url: String, method: (Request.Builder) -> Unit): Response {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .apply { method(this) }
        .build()
    return client.newCall(request).execute()
}

fun String.toJson(): JsonElement =
    gson.fromJson(this, JsonElement::class.java)

fun JsonElement.asString(): String =
    gson.toJson(this)

fun Any.toRequestBody(): RequestBody =
    gson.toJson(this).toRequestBody(
        "application/json".toMediaType()
    )

fun readJson(file: String): JsonElement {
    val reader = JsonReader(FileReader("src/integrationTest/resources/$file"))
    return gson.fromJson(reader, JsonElement::class.java)
}