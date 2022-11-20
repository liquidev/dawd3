package net.liquidev.dawd3.datagen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.util.math.Vec3f

class JsonObjectBuilder {
    private val obj = JsonObject()

    fun add(key: String, value: Boolean) =
        obj.add(key, JsonPrimitive(value))

    fun add(key: String, value: Int) =
        obj.add(key, JsonPrimitive(value))

    fun add(key: String, value: Float) =
        obj.add(key, JsonPrimitive(value))

    fun add(key: String, value: String) =
        obj.add(key, JsonPrimitive(value))

    fun add(key: String, value: JsonObject) =
        obj.add(key, value)

    fun add(key: String, value: JsonArray) =
        obj.add(key, value)

    fun build(): JsonObject = obj
}

class JsonArrayBuilder {
    private val array = JsonArray()

    fun add(value: Boolean) =
        array.add(JsonPrimitive(value))

    fun add(value: Int) =
        array.add(JsonPrimitive(value))

    fun add(value: Float) =
        array.add(JsonPrimitive(value))

    fun add(value: String) =
        array.add(JsonPrimitive(value))

    fun add(value: JsonObject) =
        array.add(value)

    fun add(value: JsonArray) =
        array.add(value)

    fun build(): JsonArray = array
}

fun jsonObject(build: JsonObjectBuilder.() -> Unit): JsonObject {
    val builder = JsonObjectBuilder()
    with(builder, build)
    return builder.build()
}

fun jsonArray(build: JsonArrayBuilder.() -> Unit): JsonArray {
    val builder = JsonArrayBuilder()
    with(builder, build)
    return builder.build()
}

fun Vec3f.toJson(): JsonArray {
    return jsonArray {
        add(x)
        add(y)
        add(z)
    }
}
