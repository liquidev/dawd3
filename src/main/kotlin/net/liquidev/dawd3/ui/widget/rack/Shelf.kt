package net.liquidev.dawd3.ui.widget.rack

import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.render.TextureStrip
import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.Message
import net.liquidev.dawd3.ui.RackScreen
import net.liquidev.dawd3.ui.widget.Widget
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.max

class Shelf(x: Float, y: Float, override var width: Float) : Widget<Nothing, Message>(x, y) {
    override var height = 128f
        private set

    val windows = mutableListOf<Window>()

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        for (window in windows) {
            window.draw(matrices, mouseX, mouseY, deltaTime)
        }
        Render.colorized(1f, 1f, 1f, 0.1f) {
            Render.line(
                matrices,
                0f,
                height,
                width,
                height,
                thickness = 0.5f,
                RackScreen.atlas,
                spacer
            )
        }
    }

    // NOTE: Events here are not handled directly by the Shelf, but rather by the parent rack
    // screen which has extra context that needs to be passed down to windows. Hence this cannot
    // be called (context is Nothing.)
    override fun event(context: Nothing, event: Event): Message {
        return Message.eventIgnored
    }

    override fun reflow() {
        height = padding * 2f + (windows.maxOfOrNull { it.height } ?: 0f)
        height = max(64f, height)

        var dx = 0f
        for (window in windows) {
            window.x = padding + dx
            window.y = padding
            dx += window.width + spacingBetweenWindows
        }
    }

    internal fun findDragDestination(mouseX: Float, mouseY: Float): Int? {
        return if (containsRelativePoint(mouseX, mouseY)) {
            val maybeIndex = windows.indexOfFirst { mouseX < it.x + it.width / 2f }
            if (maybeIndex == -1) windows.size else maybeIndex
        } else {
            null
        }
    }

    companion object {
        const val padding = 8f
        const val spacingBetweenWindows = 8f

        private val spacer = TextureStrip(25f, 16f, 25f, 32f)
    }
}