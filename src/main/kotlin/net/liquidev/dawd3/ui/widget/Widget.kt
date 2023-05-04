package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.EventContext
import net.minecraft.client.util.math.MatrixStack

abstract class Widget(var x: Float, var y: Float) {
    abstract val width: Float
    abstract val height: Float

    protected abstract fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    )

    /** Returns false to propagate the event, or true to consume it. */
    abstract fun event(context: EventContext, event: Event): Boolean

    fun draw(matrices: MatrixStack, mouseX: Float, mouseY: Float, deltaTime: Float) {
        matrices.push()
        matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        drawContent(matrices, mouseX - x, mouseY - y, deltaTime)
        matrices.pop()
    }

    fun containsRelativePoint(x: Float, y: Float) =
        x >= 0 && y >= 0 && x <= width && y <= height

    companion object {
        /** Propagates the event through the given iterable, returns whether it was consumed in the end. */
        fun propagateEvent(
            context: EventContext,
            event: Event,
            through: Iterable<Widget>,
        ): Boolean {
            for (widget in through) {
                if (
                    widget.event(
                        context,
                        event.relativeTo(widget.x, widget.y)
                    )
                ) {
                    return true
                }
            }
            return false
        }
    }
}