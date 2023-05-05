package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.render.NinePatch
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.ui.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class Window(
    x: Float,
    y: Float,
    override val width: Float,
    override val height: Float,
    val title: Text,
) : Container<DeviceEventContext>(x, y) {

    override val children = mutableListOf<DeviceWidget>()

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        Render.ninePatch(matrices, 2f, 2f, width, height, RackScreen.atlas, windowShadow)
        Render.ninePatch(matrices, 0f, 0f, width, height, RackScreen.atlas, windowBackground)

        Render.textCentered(
            matrices,
            width / 2,
            4f,
            title,
            0x111111
        )

        super.drawContent(matrices, mouseX, mouseY, deltaTime)
    }

    override fun event(context: DeviceEventContext, event: Event): Message {
        val childrenMessage = super.event(context, event)
        if (childrenMessage.eventConsumed) {
            return childrenMessage
        }

        if (event is MouseButton && event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (containsRelativePoint(event.mouseX, event.mouseY) && event.action == Action.Down) {
                return BeginDrag(this, event.mouseX, event.mouseY)
            }
        }

        return Message.eventIgnored
    }

    data class BeginDrag(
        val window: Window,
        val x: Float,
        val y: Float,
    ) : Message(eventConsumed = true)

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