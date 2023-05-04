package net.liquidev.dawd3.render

data class Tooltip(val ninePatch: NinePatch, val textColor: Int) {
    companion object {
        val dark = Tooltip(
            NinePatch(u = 32f, v = 0f, width = 8f, height = 8f, border = 2f),
            textColor = 0xFFFFFF
        )
    }
}