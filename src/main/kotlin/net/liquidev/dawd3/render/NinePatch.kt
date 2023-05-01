package net.liquidev.dawd3.render

data class NinePatch(
    val u: Int,
    val v: Int,
    val width: Int,
    val height: Int,
    val borderTop: Int,
    val borderBottom: Int,
    val borderLeft: Int,
    val borderRight: Int,
) {
    constructor(u: Int, v: Int, width: Int, height: Int, border: Int) : this(
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