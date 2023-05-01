package net.liquidev.dawd3.ui

import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

sealed interface Event {
    fun relativeTo(x: Double, y: Double): Event
}

enum class Action {
    Down,
    Up,
}

data class MouseButton(
    val action: Action,
    val mouseX: Double,
    val mouseY: Double,
    val absoluteMouseX: Double,
    val absoluteMouseY: Double,
    val button: Int,
) : Event {
    constructor(action: Action, mouseX: Double, mouseY: Double, button: Int) : this(
        action,
        mouseX,
        mouseY,
        mouseX,
        mouseY,
        button
    )

    override fun relativeTo(x: Double, y: Double) =
        MouseButton(action, mouseX - x, mouseY - y, absoluteMouseX, absoluteMouseY, button)
}

data class MouseMove(
    val mouseX: Double,
    val mouseY: Double,
    val absoluteMouseX: Double,
    val absoluteMouseY: Double,
) : Event {
    constructor(mouseX: Double, mouseY: Double) : this(mouseX, mouseY, mouseX, mouseY)

    override fun relativeTo(x: Double, y: Double): Event =
        MouseMove(mouseX - x, mouseY - y, absoluteMouseX, absoluteMouseY)
}

class EventContext(
    val world: ClientWorld,
    val blockPosition: BlockPos,
)
