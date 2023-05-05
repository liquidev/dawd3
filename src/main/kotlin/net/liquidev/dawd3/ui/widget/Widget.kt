package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.ui.DeviceEventContext
import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.Message
import net.minecraft.client.util.math.MatrixStack

abstract class Widget<in C, out M : Message>(var x: Float, var y: Float) {
    abstract val width: Float
    abstract val height: Float

    protected abstract fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    )

    abstract fun event(context: C, event: Event): M

    inline fun drawInside(matrices: MatrixStack, draw: () -> Unit) {
        matrices.push()
        matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        draw()
        matrices.pop()
    }

    fun draw(matrices: MatrixStack, mouseX: Float, mouseY: Float, deltaTime: Float) {
        drawInside(matrices) { drawContent(matrices, mouseX - x, mouseY - y, deltaTime) }
    }

    /** Can be overridden to reflow the children's layout. */
    open fun reflow() {}

    fun containsRelativePoint(x: Float, y: Float) =
        x >= 0 && y >= 0 && x <= width && y <= height

    companion object {
        /** Propagates the event through the given iterable, returns whether it was consumed in the end. */
        fun <C> propagateEvent(
            context: C,
            event: Event,
            through: Iterable<Widget<C, Message>>,
        ): Message {
            for (widget in through) {
                val message = widget.event(
                    context,
                    event.relativeTo(widget.x, widget.y)
                )
                if (message.eventConsumed) {
                    return message
                }
            }
            return Message.eventIgnored
        }
    }
}

typealias DeviceWidget = Widget<DeviceEventContext, Message>
