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
    fun icon(
        matrices: MatrixStack,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        atlas: Atlas,
        icon: Icon,
    ) {
        RenderSystem.setShaderTexture(0, atlas.asset)
        DrawableHelper.drawTexture(
            matrices,
            x,
            y,
            width,
            height,
            icon.u.toFloat(),
            icon.v.toFloat(),
            icon.width,
            icon.height,
            atlas.size,
            atlas.size
        )
    }

    fun icon(matrices: MatrixStack, x: Int, y: Int, atlas: Atlas, icon: Icon) {
        icon(matrices, x, y, icon.width, icon.height, atlas, icon)
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

    fun ninePatch(
        matrices: MatrixStack,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        atlas: Atlas,
        ninePatch: NinePatch,
    ) {
        RenderSystem.setShaderTexture(0, atlas.asset)

        // Top left
        DrawableHelper.drawTexture(
            matrices,
            x,
            y,
            ninePatch.borderLeft,
            ninePatch.borderTop,
            ninePatch.u.toFloat(),
            ninePatch.v.toFloat(),
            ninePatch.borderLeft,
            ninePatch.borderTop,
            atlas.size,
            atlas.size
        )
        // Top center
        DrawableHelper.drawTexture(
            matrices,
            x + ninePatch.borderLeft,
            y,
            width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderTop,
            (ninePatch.u + ninePatch.borderLeft).toFloat(),
            ninePatch.v.toFloat(),
            ninePatch.width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderTop,
            atlas.size,
            atlas.size
        )
        // Top right
        DrawableHelper.drawTexture(
            matrices,
            x + width - ninePatch.borderRight,
            y,
            ninePatch.borderRight,
            ninePatch.borderTop,
            (ninePatch.u + ninePatch.width - ninePatch.borderRight).toFloat(),
            ninePatch.v.toFloat(),
            ninePatch.borderRight,
            ninePatch.borderTop,
            atlas.size,
            atlas.size
        )
        // Middle left
        DrawableHelper.drawTexture(
            matrices,
            x,
            y + ninePatch.borderTop,
            ninePatch.borderLeft,
            height - ninePatch.borderTop - ninePatch.borderBottom,
            ninePatch.u.toFloat(),
            (ninePatch.v + ninePatch.borderTop).toFloat(),
            ninePatch.borderLeft,
            ninePatch.height - ninePatch.borderTop - ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Center
        DrawableHelper.drawTexture(
            matrices,
            x + ninePatch.borderLeft,
            y + ninePatch.borderTop,
            width - ninePatch.borderLeft - ninePatch.borderRight,
            height - ninePatch.borderTop - ninePatch.borderBottom,
            (ninePatch.u + ninePatch.borderLeft).toFloat(),
            (ninePatch.v + ninePatch.borderTop).toFloat(),
            ninePatch.width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.height - ninePatch.borderTop - ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Middle right
        DrawableHelper.drawTexture(
            matrices,
            x + width - ninePatch.borderRight,
            y + ninePatch.borderTop,
            ninePatch.borderLeft,
            height - ninePatch.borderTop - ninePatch.borderBottom,
            (ninePatch.u + ninePatch.width - ninePatch.borderRight).toFloat(),
            (ninePatch.v + ninePatch.borderTop).toFloat(),
            ninePatch.borderLeft,
            ninePatch.height - ninePatch.borderTop - ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Bottom left
        DrawableHelper.drawTexture(
            matrices,
            x,
            y + height - ninePatch.borderBottom,
            ninePatch.borderLeft,
            ninePatch.borderBottom,
            ninePatch.u.toFloat(),
            (ninePatch.v + ninePatch.height - ninePatch.borderBottom).toFloat(),
            ninePatch.borderLeft,
            ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Bottom center
        DrawableHelper.drawTexture(
            matrices,
            x + ninePatch.borderLeft,
            y + height - ninePatch.borderBottom,
            width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderBottom,
            (ninePatch.u + ninePatch.borderLeft).toFloat(),
            (ninePatch.v + ninePatch.height - ninePatch.borderBottom).toFloat(),
            ninePatch.width - ninePatch.borderLeft - ninePatch.borderRight,
            ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
        // Bottom right
        DrawableHelper.drawTexture(
            matrices,
            x + width - ninePatch.borderRight,
            y + height - ninePatch.borderBottom,
            ninePatch.borderRight,
            ninePatch.borderBottom,
            (ninePatch.u + ninePatch.width - ninePatch.borderRight).toFloat(),
            (ninePatch.v + ninePatch.height - ninePatch.borderBottom).toFloat(),
            ninePatch.borderRight,
            ninePatch.borderBottom,
            atlas.size,
            atlas.size
        )
    }

    fun textCentered(matrices: MatrixStack, centerX: Int, y: Int, text: Text, color: Int) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val textWidth = textRenderer.getWidth(text)
        val x = centerX - textWidth / 2
        textRenderer.draw(
            matrices,
            text,
            x.toFloat(),
            y.toFloat(),
            color
        )
    }
}