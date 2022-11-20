package net.liquidev.dawd3.common

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

/** 2x2 matrix. */
data class Affine2x2(
    val xx: Float,
    val xy: Float,
    val yx: Float,
    val yy: Float,
    val translateX: Float = 0f,
    val translateY: Float = 0f,
) {
    operator fun times(vec: Vec2f): Vec2f =
        Vec2f(vec.x * xx + vec.y * xy + translateX, vec.y * yx + vec.y * yy + translateY)

    fun timesXZ(vec: Vec3f): Vec3f {
        val vecXZ = Vec2f(vec.x, vec.z)
        val u = times(vecXZ)
        return Vec3f(u.x, vec.y, u.y)
    }
}