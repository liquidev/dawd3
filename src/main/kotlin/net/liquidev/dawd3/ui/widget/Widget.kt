package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.EventContext
import net.minecraft.client.util.math.MatrixStack

abstract class Widget(var x: Int, var y: Int) {
    abstract val width: Int
    abstract val height: Int

    protected abstract fun drawContent(
        matrices: MatrixStack,
        mouseX: Int,
        mouseY: Int,
        deltaTime: Float,
    )

    /** Returns false to propagate the event, or true to consume it. */
    abstract fun event(context: EventContext, event: Event): Boolean

    fun draw(matrices: MatrixStack, mouseX: Int, mouseY: Int, deltaTime: Float) {
        matrices.push()
        matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        drawContent(matrices, mouseX - x, mouseY - y, deltaTime)
        matrices.pop()
    }

    fun containsRelativePoint(x: Int, y: Int) =
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
                        event.relativeTo(widget.x.toDouble(), widget.y.toDouble())
                    )
                ) {
                    return true
                }
            }
            return false
        }
    }
}