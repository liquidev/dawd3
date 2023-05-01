package net.liquidev.dawd3.common

import net.minecraft.util.math.*
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

operator fun Vec2f.plus(other: Vec2f): Vec2f = Vec2f(x + other.x, y + other.y)
operator fun Vec2f.minus(other: Vec2f): Vec2f = Vec2f(x - other.x, y - other.y)
operator fun Vec2f.times(other: Vec2f): Vec2f = Vec2f(x * other.x, y * other.y)
operator fun Vec2f.div(other: Vec2f): Vec2f = Vec2f(x / other.x, y / other.y)
operator fun Vec2f.div(other: Float): Vec2f = Vec2f(x / other, y / other)

fun Vec2f.format() = "($x, $y)"

operator fun Vec3f.plus(other: Vec3f): Vec3f = Vec3f(x + other.x, y + other.y, z + other.z)
operator fun Vec3f.minus(other: Vec3f): Vec3f = Vec3f(x - other.x, y - other.y, z - other.z)
operator fun Vec3f.times(other: Vec3f): Vec3f = Vec3f(x * other.x, y * other.y, z * other.z)
operator fun Vec3f.times(other: Float): Vec3f = Vec3f(x * other, y * other, z * other)
operator fun Vec3f.div(other: Vec3f): Vec3f = Vec3f(x / other.x, y / other.y, z / other.z)

fun Vec3f.min(other: Vec3f): Vec3f = Vec3f(min(x, other.x), min(y, other.y), min(z, other.z))
fun Vec3f.max(other: Vec3f): Vec3f = Vec3f(max(x, other.x), max(y, other.y), max(z, other.z))

val Vec3f.lengthSquared get() = x * x + y * y + z * z
val Vec3f.length get() = sqrt(lengthSquared)

operator fun Vec3d.plus(other: Vec3d): Vec3d = Vec3d(x + other.x, y + other.y, z + other.z)
operator fun Vec3d.minus(other: Vec3d): Vec3d = Vec3d(x - other.x, y - other.y, z - other.z)
operator fun Vec3d.times(other: Vec3d): Vec3d = Vec3d(x * other.x, y * other.y, z * other.z)
operator fun Vec3d.div(other: Vec3d): Vec3d = Vec3d(x / other.x, y / other.y, z / other.z)

operator fun BlockPos.plus(other: BlockPos): BlockPos =
    BlockPos(x + other.x, y + other.y, z + other.z)

operator fun BlockPos.minus(other: BlockPos): BlockPos =
    BlockPos(x - other.x, y - other.y, z - other.z)

private object PlaneMatrices3Dto2D {
    val xyPlane = Mat2x3f(1f, 0f, 0f, 0f, 1f, 0f)
    val zyPlane = Mat2x3f(0f, 0f, 1f, 0f, 1f, 0f)
    val xzPlane = Mat2x3f(1f, 0f, 0f, 0f, 0f, 1f)
}

val Direction.to2DPlane
    get() =
        when (this) {
            Direction.DOWN -> PlaneMatrices3Dto2D.xzPlane
            Direction.UP -> PlaneMatrices3Dto2D.xzPlane
            Direction.NORTH -> PlaneMatrices3Dto2D.xyPlane
            Direction.SOUTH -> PlaneMatrices3Dto2D.xyPlane
            Direction.WEST -> PlaneMatrices3Dto2D.zyPlane
            Direction.EAST -> PlaneMatrices3Dto2D.zyPlane
        }

private object CorrectionMatrices {
    val invertXandY = Affine2x2f(-1f, 0f, 0f, -1f, translateX = 1f, translateY = 1f)
    val invertYOnly = Affine2x2f(1f, 0f, 0f, -1f, translateX = 0f, translateY = 1f)
}

enum class HorizontalDirection(
    val index: Int,
    val x: Int,
    val z: Int,
    val angle: Float,
    val direction: Direction,
) {
    East(0, x = 1, z = 0, angle = 0f, direction = Direction.EAST),
    South(1, x = 0, z = 1, angle = 0.5f * PI.toFloat(), direction = Direction.SOUTH),
    West(2, x = -1, z = 0, angle = PI.toFloat(), direction = Direction.WEST),
    North(3, x = 0, z = -1, angle = 1.5f * PI.toFloat(), direction = Direction.NORTH);

    operator fun plus(other: HorizontalDirection): HorizontalDirection =
        fromIndex((index + other.index) % values.size)

    operator fun minus(other: HorizontalDirection): HorizontalDirection =
        fromIndex((index - other.index).mod(values.size))

    fun clockwise() = plus(South)
    fun counterClockwise() = minus(South)

    fun rotateY(vec: Vec3f): Vec3f =
        when (this) {
            East -> vec
            North -> Vec3f(vec.z, vec.y, -vec.x)
            West -> Vec3f(-vec.x, vec.y, -vec.z)
            South -> Vec3f(-vec.z, vec.y, vec.x)
        }

    val faceCorrection
        get() =
            when (this) {
                East -> CorrectionMatrices.invertXandY
                South -> CorrectionMatrices.invertYOnly
                West -> CorrectionMatrices.invertYOnly
                North -> CorrectionMatrices.invertXandY
            }

    val vector get() = Vec3i(x, 0, z)

    companion object {
        private val values = values()

        fun fromIndex(i: Int) = values[i]

        fun fromDirection(direction: Direction): HorizontalDirection? =
            when (direction) {
                Direction.EAST -> East
                Direction.SOUTH -> South
                Direction.WEST -> West
                Direction.NORTH -> North
                else -> null
            }
    }
}

fun Vec3d.toVec3f() = Vec3f(x.toFloat(), y.toFloat(), z.toFloat())
fun Vec3f.toVec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
fun Vec3i.toVec3f() = Vec3f(x.toFloat(), y.toFloat(), z.toFloat())
fun BlockPos.toVec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun pointInRectangle(point: Vec2f, topLeft: Vec2f, bottomRight: Vec2f) =
    point.x >= topLeft.x && point.y >= topLeft.y &&
        point.x <= bottomRight.x && point.y <= bottomRight.y

fun lerp(a: Float, b: Float, t: Float): Float =
    a + t * (b - a)

fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float =
    toMin + (value - fromMin) / (fromMax - fromMin) * (toMax - toMin)

fun clamp(value: Float, min: Float, max: Float): Float =
    max(min(value, max), min)

object Cuboids {
    val fullBlock = Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    val halfBlock = Box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)

    fun newF(from: Vec3f, to: Vec3f) = Box(from.toVec3d(), to.toVec3d())
}

val Box.fromF get() = Vec3f(minX.toFloat(), minY.toFloat(), minZ.toFloat())
val Box.toF get() = Vec3f(maxX.toFloat(), maxY.toFloat(), maxZ.toFloat())

@JvmInline
value class Radians(val value: Float) {
    fun toDegrees() =
        Degrees(value / PI.toFloat() * 180f)
}

@JvmInline
value class Degrees(val value: Float) {
    fun toRadians() =
        Radians(value / 180f * PI.toFloat())
}
