package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.Message
import net.minecraft.client.util.math.MatrixStack

abstract class Container<in C>(x: Float, y: Float) : Widget<C, Message>(x, y) {
    abstract val children: List<Widget<C, Message>>

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        for (child in children) {
            child.draw(matrices, mouseX, mouseY, deltaTime)
        }
    }

    override fun event(context: C, event: Event): Message {
        return propagateEvent(context, event, children.reversed())
    }
}