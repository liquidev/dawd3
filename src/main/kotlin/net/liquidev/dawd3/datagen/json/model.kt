package net.liquidev.dawd3.datagen.json

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.liquidev.dawd3.datagen.jsonArray
import net.liquidev.dawd3.datagen.jsonObject
import net.liquidev.dawd3.datagen.toJson
import net.minecraft.util.math.Vec3f

fun element(
    from: Vec3f,
    to: Vec3f,
    faces: JsonObject,
): JsonObject {
    return jsonObject {
        add("from", from.toJson())
        add("to", to.toJson())
        add("faces", faces)
    }
}

fun faces(
    north: JsonObject? = null,
    east: JsonObject? = null,
    south: JsonObject? = null,
    west: JsonObject? = null,
    up: JsonObject? = null,
    down: JsonObject? = null,
): JsonObject {
    return jsonObject {
        if (north != null) add("north", north)
        if (east != null) add("east", east)
        if (south != null) add("south", south)
        if (west != null) add("west", west)
        if (up != null) add("up", up)
        if (down != null) add("down", down)
    }
}

fun uvRect(x1: Float, y1: Float, x2: Float, y2: Float): JsonArray {
    return jsonArray {
        add(x1)
        add(y1)
        add(x2)
        add(y2)
    }
}

fun face(
    uv: JsonArray? = null,
    texture: String,
    cullface: String? = null,
    rotation: Int? = null,
): JsonObject {
    return jsonObject {
        if (uv != null) add("uv", uv)
        add("texture", texture)
        if (cullface != null) add("cullface", cullface)
        if (rotation != null) add("rotation", rotation)
    }
}
