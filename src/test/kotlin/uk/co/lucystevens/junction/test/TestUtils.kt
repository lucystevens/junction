package uk.co.lucystevens.junction.test

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.FileReader
import java.sql.ResultSet

val gson = GsonBuilder().setPrettyPrinting().create();

fun OkHttpClient.doRequest(url: String, method: (Request.Builder) -> Unit): Response {
    val request = Request.Builder()
        .url(url)
        .apply { method(this) }
        .build()
    return newCall(request).execute()
}

fun String.toJson(): JsonElement =
    gson.fromJson(this, JsonElement::class.java)

fun JsonElement.asString(): String =
    gson.toJson(this)

fun Any.toRequestBody(): RequestBody =
    gson.toJson(this).toRequestBody(
        "application/json".toMediaType()
    )

inline fun <reified T> ResultSet.getJson(column: String) =
    getString(column).parse<T>()

inline fun <reified T> String.parse(): T {
    return Json.decodeFromString(this)
}

fun readJson(testType: String, file: String): JsonElement {
    val reader = JsonReader(FileReader("src/$testType/resources/$file"))
    return gson.fromJson(reader, JsonElement::class.java)
}