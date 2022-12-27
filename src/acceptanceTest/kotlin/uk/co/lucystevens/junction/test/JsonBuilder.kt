package uk.co.lucystevens.junction.test

import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun jsonObject(builder: JsonObjectBuilder.() -> Unit) =
    JsonObjectBuilder().apply(builder).json

fun jsonArray(builder: JsonArrayBuilder.() -> Unit) =
    JsonArrayBuilder().apply(builder).json

class JsonObjectBuilder {
    val json = JsonObject()
    fun prop(name: String, value: Boolean) = json.addProperty(name, value)
    fun prop(name: String, value: Char) = json.addProperty(name, value)
    fun prop(name: String, value: Number) = json.addProperty(name, value)
    fun prop(name: String, value: String) = json.addProperty(name, value)
    fun obj(name: String, builder: JsonObjectBuilder.() -> Unit) =
        json.add(name, jsonObject(builder))
    fun array(name: String, builder: JsonArrayBuilder.() -> Unit) =
        json.add(name, jsonArray(builder))
}

class JsonArrayBuilder {
    val json = JsonArray()
    fun add(value: Boolean) = json.add(value)
    fun add(value: Char) = json.add(value)
    fun add(value: Number) = json.add(value)
    fun add(value: String) = json.add(value)
    fun obj(builder: JsonObjectBuilder.() -> Unit) =
        json.add(jsonObject(builder))
    fun array(builder: JsonArrayBuilder.() -> Unit) =
        json.add(jsonArray(builder))
}