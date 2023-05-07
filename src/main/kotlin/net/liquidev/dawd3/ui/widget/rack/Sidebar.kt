package net.liquidev.dawd3.ui.widget.rack

import net.liquidev.dawd3.render.NinePatch
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.Message
import net.liquidev.dawd3.ui.RackScreen
import net.liquidev.dawd3.ui.widget.Widget
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.util.math.MatrixStack

class Sidebar(
    x: Float,
    y: Float,
    override var width: Float,
    override var height: Float,
) : Widget<Nothing, Message>(x, y) {

    val windows = mutableListOf<Window>()

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        Render.ninePatch(matrices, 0f, 0f, width, height, RackScreen.atlas, background)

        for (window in windows) {
            window.draw(matrices, mouseX, mouseY, deltaTime)
        }
    }

    // NOTE: Events here are not handled directly by the Sidebar, but rather by the parent rack
    // screen which has extra context that needs to be passed down to windows. Hence this cannot
    // be called (context is Nothing.)
    override fun event(context: Nothing, event: Event): Message {
        return Message.eventIgnored
    }

    override fun reflow() {
        var dy = 0f
        for (window in windows) {
            window.x = padding
            window.y = padding + dy
            dy += window.height + spacingBetweenWindows
        }
    }

    internal fun findDragDestination(mouseX: Float, mouseY: Float): Int? {
        return if (containsRelativePoint(mouseX, mouseY)) {
            val maybeIndex = windows.indexOfFirst { mouseY < it.y + it.height / 2f }
            if (maybeIndex == -1) windows.size else maybeIndex
        } else {
            null
        }
    }

    companion object {
        val background = NinePatch(u = 40f, v = 0f, width = 8f, height = 8f, border = 2f)

        const val padding = 8f
        const val spacingBetweenWindows = 8f
    }
}