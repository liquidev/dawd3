package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.render.NinePatch
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.ui.Rack
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class Window(
    x: Int,
    y: Int,
    override val width: Int,
    override val height: Int,
    val title: Text,
) : Container(x, y) {

    override val children = mutableListOf<Widget>()

    override fun drawContent(matrices: MatrixStack, mouseX: Int, mouseY: Int, deltaTime: Float) {
        Render.ninePatch(matrices, 2, 2, width, height, Rack.atlas, windowShadow)
        Render.ninePatch(matrices, 0, 0, width, height, Rack.atlas, windowBackground)

        Render.textCentered(
            matrices,
            width / 2,
            4,
            title,
            0x111111
        )

        super.drawContent(matrices, mouseX, mouseY, deltaTime)
    }

    companion object {
        val windowBackground = NinePatch(
            u = 0, v = 0,
            width = 16, height = 16,
            border = 3
        )
        val windowShadow = NinePatch(
            u = 16, v = 0,
            width = 16, height = 16,
            border = 3
        )
    }
}