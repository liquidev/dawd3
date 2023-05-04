package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.render.NinePatch
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.ui.Rack
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class Window(
    x: Float,
    y: Float,
    override val width: Float,
    override val height: Float,
    val title: Text,
) : Container(x, y) {

    override val children = mutableListOf<Widget>()

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        Render.ninePatch(matrices, 2f, 2f, width, height, Rack.atlas, windowShadow)
        Render.ninePatch(matrices, 0f, 0f, width, height, Rack.atlas, windowBackground)

        Render.textCentered(
            matrices,
            width / 2,
            4f,
            title,
            0x111111
        )

        super.drawContent(matrices, mouseX, mouseY, deltaTime)
    }

    companion object {
        val windowBackground = NinePatch(
            u = 0f, v = 0f,
            width = 16f, height = 16f,
            border = 3f
        )
        val windowShadow = NinePatch(
            u = 16f, v = 0f,
            width = 16f, height = 16f,
            border = 3f
        )
    }
}