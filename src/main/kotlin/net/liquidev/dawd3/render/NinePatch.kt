package net.liquidev.dawd3.render

data class NinePatch(
    val u: Float,
    val v: Float,
    val width: Float,
    val height: Float,
    val borderTop: Float,
    val borderBottom: Float,
    val borderLeft: Float,
    val borderRight: Float,
) {
    constructor(u: Float, v: Float, width: Float, height: Float, border: Float) : this(
        u,
        v,
        width,
        height,
        border,
        border,
        border,
        border,
    )
}