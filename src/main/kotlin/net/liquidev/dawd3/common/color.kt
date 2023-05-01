package net.liquidev.dawd3.common

fun rgba(r: Int, g: Int, b: Int, a: Int): Int =
    (a shl 24) or (r shl 16) or (g shl 8) or b

fun rgb(r: Int, g: Int, b: Int): Int = rgba(r, g, b, 255)
