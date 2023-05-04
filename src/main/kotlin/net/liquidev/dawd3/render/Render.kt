package net.liquidev.dawd3.render

import com.mojang.blaze3d.systems.RenderSystem
import net.liquidev.dawd3.common.Radians
import net.liquidev.dawd3.common.lerp
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Render {
    fun sprite(
        matrices: MatrixStack,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        atlas: Atlas,
        sprite: Sprite,
    ) {
        RenderSystem.setShaderTexture(0, atlas.asset)
        DrawableHelper.drawTexture(
            matrices,
            x.toInt(),
            y.toInt(),
            width.toInt(),
            height.toInt(),
            sprite.u,
            sprite.v,
            sprite.width.toInt(),
            sprite.height.toInt(),
            atlas.size.toInt(),
            atlas.size.toInt()
        )
    }

    fun sprite(matrices: MatrixStack, x: Float, y: Float, atlas: Atlas, icon: Sprite) {
        sprite(matrices, x, y, icon.width, icon.height, atlas, icon)
    }

    fun line(
        matrices: MatrixStack,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        thickness: Float,
        atlas: Atlas,
        textureStrip: TextureStrip,
    ) {
        if (x1 == x2 && y1 == y2) return

        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, atlas.asset)

        val matrix = matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().buffer

        val dx = x2 - x1
        val dy = y2 - y1
        val length = sqrt(dx * dx + dy * dy)
        val ndx = dx / length
        val ndy = dy / length

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE)

        buffer.vertex(matrix, x2 - ndy * thickness, y2 + ndx * thickness, 0f)
        buffer.texture(textureStrip.u1 / atlas.size, textureStrip.v2 / atlas.size)
        buffer.next()

        buffer.vertex(matrix, x2 + ndy * thickness, y2 - ndx * thickness, 0f)
        buffer.texture(textureStrip.u2 / atlas.size, textureStrip.v2 / atlas.size)
        buffer.next()

        buffer.vertex(matrix, x1 - ndy * thickness, y1 + ndx * thickness, 0f)
        buffer.texture(textureStrip.u1 / atlas.size, textureStrip.v1 / atlas.size)
        buffer.next()

        buffer.vertex(matrix, x1 + ndy * thickness, y1 - ndx * thickness, 0f)
        buffer.texture(textureStrip.u2 / atlas.size, textureStrip.v1 / atlas.size)
        buffer.next()


        Tessellator.getInstance().draw()
    }

    fun arcOutline(
        matrices: MatrixStack,
        centerX: Float,
        centerY: Float,
        radius: Float,
        thickness: Float,
        startAngle: Radians,
        endAngle: Radians,
        vertexCount: Int,
        atlas: Atlas,
        textureStrip: TextureStrip,
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, atlas.asset)

        val matrix = matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().buffer

        // delta is the angle between subdivisions of the circle.
        val delta = (endAngle.value - startAngle.value) / vertexCount
        // alpha is the isosceles trapezoids' acute angle, which we need to correct the thickness
        // and create a proper mitre joint.
        val alpha = (PI.toFloat() - delta) / 2
        val offset = thickness / sin(alpha)
        val innerRadius = radius - offset / 2

        val cos = cos(delta)
        val sin = sin(delta)
        var angleX = cos(startAngle.value)
        var angleY = sin(startAngle.value)

        val u1 = textureStrip.u1 / atlas.size
        val u2 = textureStrip.u2 / atlas.size

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE)
        for (i in 0..vertexCount) {
            val innerX = centerX + angleX * innerRadius
            val innerY = centerY + angleY * innerRadius
            val outerX = centerX + angleX * (innerRadius + offset)
            val outerY = centerY + angleY * (innerRadius + offset)

            val t = i.toFloat() / vertexCount
            val v = lerp(textureStrip.v1, textureStrip.v2, t) / atlas.size

            buffer.vertex(matrix, outerX, outerY, 0f)
            buffer.texture(u2, v)
            buffer.next()
            buffer.vertex(matrix, innerX, innerY, 0f)
            buffer.texture(u1, v)
            buffer.next()

            val aX = angleX
            val aY = angleY
            angleX = aX * cos - aY * sin
            angleY = aX * sin + aY * cos
        }
        Tessellator.getInstance().draw()
    }

    private fun drawTexture(
        matrices: MatrixStack,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        u: Float,
        v: Float,
        regionWidth: Float,
        regionHeight: Float,
        textureWidth: Float,
        textureHeigh: Float,
    ) {
        // Temporarily defined to just defer to DrawableHelper, later on we should introduce support
        // for rendering at floating-point coordinates.
        DrawableHelper.drawTexture(
            matrices,
            x.toInt(),
            y.toInt(),
            width.toInt(),
            height.toInt(),
            u,
            v,
            regionWidth.toInt(),
            regionHeight.toInt(),
            textureWidth.toInt(),
            textureHeigh.toInt()
        )
    }

    fun ninePatch(
        matrices: MatrixStack,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        atlas: Atlas,
        ninePatch: NinePatch,
    ) {
        RenderSystem.setShaderTexture(0, atlas.asset)

        // Top left
        drawTexture(
            matrices,
            x,
            y,
            ninePatch.borderLeft,
            ninePatch.borderTop,
            ninePatch.u,
            ninePatch.v,
            ninePatch.borderLeft,
            ninePatch.borderTop,
            atlas.size,
            atlas.size
        )
        // Top center
        drawTexture(
            matrices,
            x + ninePatch.borderLeft,
            y,
            width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderTop,
            ninePatch.u + ninePatch.borderLeft,
            ninePatch.v,
            ninePatch.width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderTop,
            atlas.size,
            atlas.size
        )
        // Top right
        drawTexture(
            matrices,
            x + width - ninePatch.borderRight,
            y,
            ninePatch.borderRight,
            ninePatch.borderTop,
            ninePatch.u + ninePatch.width - ninePatch.borderRight,
            ninePatch.v,
            ninePatch.borderRight,
            ninePatch.borderTop,
            atlas.size,
            atlas.size
        )
        // Middle left
        drawTexture(
            matrices,
            x,
            y + ninePatch.borderTop,
            ninePatch.borderLeft,
            height - ninePatch.borderTop - ninePatch.borderBottom,
            ninePatch.u,
            ninePatch.v + ninePatch.borderTop,
            ninePatch.borderLeft,
            ninePatch.height - ninePatch.borderTop - ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Center
        drawTexture(
            matrices,
            x + ninePatch.borderLeft,
            y + ninePatch.borderTop,
            width - ninePatch.borderLeft - ninePatch.borderRight,
            height - ninePatch.borderTop - ninePatch.borderBottom,
            ninePatch.u + ninePatch.borderLeft,
            ninePatch.v + ninePatch.borderTop,
            ninePatch.width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.height - ninePatch.borderTop - ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Middle right
        drawTexture(
            matrices,
            x + width - ninePatch.borderRight,
            y + ninePatch.borderTop,
            ninePatch.borderLeft,
            height - ninePatch.borderTop - ninePatch.borderBottom,
            ninePatch.u + ninePatch.width - ninePatch.borderRight,
            ninePatch.v + ninePatch.borderTop,
            ninePatch.borderLeft,
            ninePatch.height - ninePatch.borderTop - ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Bottom left
        drawTexture(
            matrices,
            x,
            y + height - ninePatch.borderBottom,
            ninePatch.borderLeft,
            ninePatch.borderBottom,
            ninePatch.u,
            ninePatch.v + ninePatch.height - ninePatch.borderBottom,
            ninePatch.borderLeft,
            ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Bottom center
        drawTexture(
            matrices,
            x + ninePatch.borderLeft,
            y + height - ninePatch.borderBottom,
            width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderBottom,
            ninePatch.u + ninePatch.borderLeft,
            ninePatch.v + ninePatch.height - ninePatch.borderBottom,
            ninePatch.width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Bottom right
        drawTexture(
            matrices,
            x + width - ninePatch.borderRight,
            y + height - ninePatch.borderBottom,
            ninePatch.borderRight,
            ninePatch.borderBottom,
            ninePatch.u + ninePatch.width - ninePatch.borderRight,
            ninePatch.v + ninePatch.height - ninePatch.borderBottom,
            ninePatch.borderRight,
            ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
    }

    fun textWidth(text: Text): Float {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        return textRenderer.getWidth(text).toFloat()
    }

    fun text(matrices: MatrixStack, x: Float, y: Float, text: Text, color: Int) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        textRenderer.draw(matrices, text, x, y, color)
    }

    fun textCentered(matrices: MatrixStack, centerX: Float, y: Float, text: Text, color: Int) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val textWidth = textRenderer.getWidth(text)
        val x = centerX - textWidth / 2
        text(matrices, x, y, text, color)
    }

    fun tooltipWidth(text: Text): Float = textWidth(text) + 5f

    fun tooltip(
        matrices: MatrixStack,
        x: Float,
        y: Float,
        text: Text,
        atlas: Atlas,
        tooltip: Tooltip,
    ) {
        val width = tooltipWidth(text)
        ninePatch(matrices, x, y, width, height = 14f, atlas, tooltip.ninePatch)
        text(matrices, x + 3, y + 3, text, tooltip.textColor)
    }

    fun tooltipCentered(
        matrices: MatrixStack,
        centerX: Float,
        y: Float,
        text: Text,
        atlas: Atlas,
        tooltip: Tooltip,
    ) {
        val width = tooltipWidth(text)
        val x = centerX - width / 2
        tooltip(matrices, x, y, text, atlas, tooltip)
    }
}