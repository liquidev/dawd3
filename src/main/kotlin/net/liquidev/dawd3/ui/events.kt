package net.liquidev.dawd3.ui

import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

sealed interface Event {
    fun relativeTo(x: Float, y: Float): Event
}

enum class Action {
    Down,
    Up,
}

data class MouseButton(
    val action: Action,
    val mouseX: Float,
    val mouseY: Float,
    val absoluteMouseX: Float,
    val absoluteMouseY: Float,
    val button: Int,
) : Event {
    constructor(action: Action, mouseX: Float, mouseY: Float, button: Int) : this(
        action,
        mouseX,
        mouseY,
        mouseX,
        mouseY,
        button
    )

    override fun relativeTo(x: Float, y: Float) =
        MouseButton(action, mouseX - x, mouseY - y, absoluteMouseX, absoluteMouseY, button)
}

data class MouseMove(
    val mouseX: Float,
    val mouseY: Float,
    val absoluteMouseX: Float,
    val absoluteMouseY: Float,
) : Event {
    constructor(mouseX: Float, mouseY: Float) : this(mouseX, mouseY, mouseX, mouseY)

    override fun relativeTo(x: Float, y: Float): Event =
        MouseMove(mouseX - x, mouseY - y, absoluteMouseX, absoluteMouseY)
}

class EventContext(
    val world: ClientWorld,
    val blockPosition: BlockPos,
)
