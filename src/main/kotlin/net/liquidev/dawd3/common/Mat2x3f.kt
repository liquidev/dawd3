package net.liquidev.dawd3.common

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

/** Matrix with two rows and three columns. */
class Mat2x3f(
    val xx: Float,
    val xy: Float,
    val xz: Float,
    val yx: Float,
    val yy: Float,
    val yz: Float,
) {
    operator fun times(vec: Vec3f) =
        Vec2f(
            vec.x * xx + vec.y * xy + vec.z * xz,
            vec.x * yx + vec.y * yy + vec.z * yz
        )
}