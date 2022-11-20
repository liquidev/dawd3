package net.liquidev.dawd3.common

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f
import kotlin.math.max
import kotlin.math.min

operator fun Vec2f.plus(other: Vec2f): Vec2f = Vec2f(x + other.x, y + other.y)
operator fun Vec2f.minus(other: Vec2f): Vec2f = Vec2f(x - other.x, y - other.y)
operator fun Vec2f.times(other: Vec2f): Vec2f = Vec2f(x * other.x, y * other.y)
operator fun Vec2f.div(other: Vec2f): Vec2f = Vec2f(x / other.x, y / other.y)

fun Vec3f.min(other: Vec3f): Vec3f = Vec3f(min(x, other.x), min(y, other.y), min(z, other.z))
fun Vec3f.max(other: Vec3f): Vec3f = Vec3f(max(x, other.x), max(y, other.y), max(z, other.z))
