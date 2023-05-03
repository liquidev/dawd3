package net.liquidev.dawd3.ui.widget

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.liquidev.dawd3.audio.device.BlockEntityControls
import net.liquidev.dawd3.audio.device.FloatControl
import net.liquidev.dawd3.common.Degrees
import net.liquidev.dawd3.common.Radians
import net.liquidev.dawd3.common.clamp
import net.liquidev.dawd3.common.mapRange
import net.liquidev.dawd3.net.ControlTweaked
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.render.TextureStrip
import net.liquidev.dawd3.render.Tooltip
import net.liquidev.dawd3.ui.*
import net.liquidev.dawd3.ui.units.FloatUnit
import net.liquidev.dawd3.ui.units.RawValue
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import kotlin.math.*

class Knob(
    x: Int,
    y: Int,
    val control: FloatControl,
    val min: Float,
    val max: Float,
    val color: Color,
    size: Int = normalSize,
    val unit: FloatUnit = RawValue,
    // Pick a default sensitivity such that for pitch ranges we move by steps of 0.25.
    val sensitivity: Float = (max - min) * 0.25f / 96f,
) : Widget(x, y) {
    override val width = size
    override val height = size + 4

    private data class DraggingInfo(var previousMouseY: Double)

    private var draggingInfo: DraggingInfo? = null

    private val controlLabel =
        Text.translatable(control.descriptor.name.toTranslationKey()).setStyle(Rack.smallText)

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

        Render.textCentered(
            matrices,
            centerX.roundToInt() + 1,
            height - 5,
            controlLabel,
            0x111111
        )

        if (draggingInfo != null) {
            Render.tooltipCentered(
                matrices,
                centerX.toInt(),
                height - 4,
                Text.literal(unit.display(control.value)),
                Rack.atlas,
                Tooltip.dark
            )
        }
    }

    override fun event(context: EventContext, event: Event): Boolean {
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
                        return true
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
                    BlockEntityControls.setFloatControlValue(
                        context.blockPosition,
                        control,
                        alterValue(control.value, by = deltaY.toFloat())
                    )
                    draggingInfo.previousMouseY = event.absoluteMouseY
                    // Consume the events so that other controls don't get triggered while the knob
                    // is being dragged.
                    return true
                }
            }
        }

        return false
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

        const val normalSize = 20
        const val smallSize = 16
    }
}