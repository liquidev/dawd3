package net.liquidev.dawd3.render

data class Tooltip(val ninePatch: NinePatch, val textColor: Int) {
    companion object {
        val dark = Tooltip(
            NinePatch(u = 32, v = 0, width = 8, height = 8, border = 2),
            textColor = 0xFFFFFF
        )
    }
}