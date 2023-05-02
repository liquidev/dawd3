package net.liquidev.dawd3.ui.widget

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.liquidev.dawd3.audio.device.Control
import net.liquidev.dawd3.common.Degrees
import net.liquidev.dawd3.common.Radians
import net.liquidev.dawd3.common.clamp
import net.liquidev.dawd3.common.mapRange
import net.liquidev.dawd3.net.ControlTweaked
import net.liquidev.dawd3.net.TweakControl
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.render.TextureStrip
import net.liquidev.dawd3.ui.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.glfw.GLFW
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Knob(
    x: Int,
    y: Int,
    val control: Control,
    val min: Float,
    val max: Float,
    val color: Color,
    // Pick a default sensitivity such that for pitch ranges we move by steps of 0.25.
    val sensitivity: Float = (max - min) * 0.25f / 96f,
) : Widget(x, y) {
    override val width = 20
    override val height = 20

    private data class DraggingInfo(var previousMouseY: Double)

    private var draggingInfo: DraggingInfo? = null

    override fun drawContent(matrices: MatrixStack, mouseX: Int, mouseY: Int, deltaTime: Float) {
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        val radius = width.toFloat() / 2

        val valueAngle = mapRange(control.value, min, max, startAngle.value, endAngle.value)
        val zeroAngle = clamp(
            mapRange(0f, min, max, startAngle.value, endAngle.value),
            startAngle.value,
            endAngle.value
        )

        Render.arcOutline(
            matrices,
            centerX,
            centerY,
            radius - lineThickness * 0.75f,
            lineThickness * 0.75f,
            Radians(min(valueAngle, zeroAngle)),
            Radians(max(valueAngle, zeroAngle)),
            vertexCount = 16,
            Rack.atlas,
            coloredStrip(color)
        )
        Render.arcOutline(
            matrices,
            centerX,
            centerY,
            radius,
            lineThickness,
            startAngle,
            endAngle,
            vertexCount = 16,
            Rack.atlas,
            blackStrip
        )
        Render.line(
            matrices,
            centerX + cos(valueAngle) * (radius * 0.25f),
            centerY + sin(valueAngle) * (radius * 0.25f),
            centerX + cos(valueAngle) * radius,
            centerY + sin(valueAngle) * radius,
            lineThickness / 2,
            Rack.atlas,
            blackStrip
        )
    }

    override fun event(context: EventContext, event: Event): Event? {
        val client = MinecraftClient.getInstance()
        when (event) {
            is MouseButton -> if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                when (event.action) {
                    Action.Down -> if (
                        containsRelativePoint(
                            event.mouseX.toInt(),
                            event.mouseY.toInt()
                        )
                    ) {
                        draggingInfo = DraggingInfo(event.absoluteMouseY)
                        val guiScale =
                            client.options.guiScale.value.toDouble()
                        InputUtil.setCursorParameters(
                            client.window.handle,
                            GLFW.GLFW_CURSOR_DISABLED,
                            event.absoluteMouseX * guiScale,
                            event.absoluteMouseY * guiScale,
                        )
                        return null
                    }
                    Action.Up -> {
                        val draggingInfo = draggingInfo
                        if (draggingInfo != null) {
                            InputUtil.setCursorParameters(
                                client.window.handle,
                                GLFW.GLFW_CURSOR_NORMAL,
                                0.0,
                                0.0,
                            )
                            ClientPlayNetworking.send(
                                ControlTweaked.id,
                                ControlTweaked(context.blockPosition).serialize()
                            )
                            this.draggingInfo = null
                        }
                    }
                }
            }
            is MouseMove -> {
                val draggingInfo = draggingInfo
                if (draggingInfo != null) {
                    val guiScale = client.options.guiScale.value.toFloat()
                    val deltaY = (draggingInfo.previousMouseY - event.absoluteMouseY) * guiScale
                    ClientPlayNetworking.send(
                        TweakControl.id,
                        TweakControl(
                            context.blockPosition,
                            control.descriptor.name.id,
                            newValue = alterValue(control.value, by = deltaY.toFloat()),
                        ).serialize()
                    )
                    // Reflect the change locally immediately for lower latency.
                    control.value = alterValue(control.value, by = deltaY.toFloat())
                    draggingInfo.previousMouseY = event.absoluteMouseY
                }
            }
        }

        return event
    }

    private fun alterValue(value: Float, by: Float): Float =
        max(min(value + by * sensitivity, max), min)

    enum class Color(val index: Int) {
        Red(0),
        Orange(1),
        Yellow(2),
        Green(3),
        Blue(4),
        Purple(5),
    }

    companion object {
        private val startAngle = Degrees(135f).toRadians()
        private val endAngle = Degrees(405f).toRadians()

        private const val lineThickness = 2f

        private val blackStrip = TextureStrip(16f, 16f, 16f, 32f)
        private fun coloredStrip(color: Color): TextureStrip {
            val u = 18f + color.index.toFloat()
            return TextureStrip(u, 16f, u, 32f)
        }
    }
}