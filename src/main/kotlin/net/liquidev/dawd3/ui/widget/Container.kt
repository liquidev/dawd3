package net.liquidev.dawd3.ui.widget

import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.EventContext
import net.minecraft.client.util.math.MatrixStack

abstract class Container(x: Int, y: Int) : Widget(x, y) {
    abstract val children: List<Widget>

    override fun drawContent(matrices: MatrixStack, mouseX: Int, mouseY: Int, deltaTime: Float) {
        for (child in children) {
            child.draw(matrices, mouseX, mouseY, deltaTime)
        }
    }

    override fun event(context: EventContext, event: Event): Boolean {
        return propagateEvent(context, event, children.reversed())
    }
}