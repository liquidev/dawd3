package net.liquidev.dawd3.common

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

/** Matrix with three columns and two rows. */
class Mat3x2f(
    val xx: Float,
    val xy: Float,
    val yx: Float,
    val yy: Float,
    val zx: Float,
    val zy: Float,
) {
    operator fun times(vec: Vec2f): Vec3f =
        Vec3f(
            vec.x * xx + vec.y * xy,
            vec.x * yx + vec.y * yy,
            vec.x * zx + vec.y * zy,
        )
}