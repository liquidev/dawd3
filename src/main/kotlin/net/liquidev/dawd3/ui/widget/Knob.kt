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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Knob(
    x: Float,
    y: Float,
    val control: FloatControl,
    val min: Float,
    val max: Float,
    val color: Color,
    size: Float = normalSize,
    val unit: FloatUnit = RawValue,
    // Pick a default sensitivity such that for pitch ranges we move by steps of 0.25.
    val sensitivity: Float = (max - min) * 0.25f / 96f,
) : DeviceWidget(x, y) {
    override val width = size
    override val height = size + 4f

    private data class DraggingInfo(var previousMouseY: Float)

    private var draggingInfo: DraggingInfo? = null

    private val controlLabel =
        Text.translatable(control.descriptor.name.toTranslationKey()).setStyle(RackScreen.smallText)

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2f

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
            RackScreen.atlas,
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
            RackScreen.atlas,
            blackStrip
        )
        Render.line(
            matrices,
            centerX + cos(valueAngle) * (radius * 0.25f),
            centerY + sin(valueAngle) * (radius * 0.25f),
            centerX + cos(valueAngle) * radius,
            centerY + sin(valueAngle) * radius,
            lineThickness / 2,
            RackScreen.atlas,
            blackStrip
        )

        Render.textCentered(
            matrices,
            centerX + 1f,
            height - 5,
            controlLabel,
            0x111111
        )

        if (draggingInfo != null) {
            Render.tooltipCentered(
                matrices,
                centerX,
                height - 4,
                Text.literal(unit.display(control.value)),
                RackScreen.atlas,
                Tooltip.dark
            )
        }
    }

    override fun event(context: DeviceEventContext, event: Event): Message {
        val client = MinecraftClient.getInstance()
        when (event) {
            is MouseButton -> if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                when (event.action) {
                    Action.Down -> if (
                        containsRelativePoint(
                            event.mouseX,
                            event.mouseY
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
                        return Message.eventUsed
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
                    var deltaY = (draggingInfo.previousMouseY - event.absoluteMouseY) * guiScale
                    if (isFineTuning()) {
                        deltaY *= 0.05f
                    }
                    BlockEntityControls.setFloatControlValue(
                        context.blockPosition,
                        control,
                        alterValue(control.value, by = deltaY)
                    )
                    draggingInfo.previousMouseY = event.absoluteMouseY
                    // Consume the events so that other controls don't get triggered while the knob
                    // is being dragged.
                    return Message.eventUsed
                }
            }
        }

        return Message.eventIgnored
    }

    private fun isFineTuning(): Boolean =
        InputUtil.isKeyPressed(
            MinecraftClient.getInstance().window.handle,
            GLFW.GLFW_KEY_LEFT_SHIFT
        )

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

        const val normalSize = 20f
    }
}