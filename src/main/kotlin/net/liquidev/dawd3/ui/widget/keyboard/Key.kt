package net.liquidev.dawd3.ui.widget.keyboard

import net.liquidev.dawd3.audio.devices.io.KeyboardDevice
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.render.Sprite
import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.EventContext
import net.liquidev.dawd3.ui.Rack
import net.liquidev.dawd3.ui.widget.Widget
import net.minecraft.client.util.math.MatrixStack

class Key(
    x: Int,
    y: Int,
    val type: Type,
    val note: Float,
    val controls: KeyboardDevice.Controls,
) : Widget(x, y) {

    enum class Type(
        val width: Int,
        val height: Int,
        val idleSprite: Sprite,
        val pressedSprite: Sprite,
    ) {
        White(
            width = 10,
            height = 32,
            idleSprite = Sprite(u = 32, v = 16, width = 10, height = 32),
            pressedSprite = Sprite(u = 42, v = 16, width = 10, height = 32),
        ),
        Black(
            width = 5,
            height = 20,
            idleSprite = Sprite(u = 52, v = 16, width = 7, height = 20),
            pressedSprite = Sprite(u = 52, v = 36, width = 7, height = 20)
        ),
    }

    override val width = type.width
    override val height = type.height

    private var isPressed = false

    override fun drawContent(matrices: MatrixStack, mouseX: Int, mouseY: Int, deltaTime: Float) {
        Render.sprite(
            matrices,
            x = 0,
            y = 0,
            Rack.atlas,
            if (isPressed) type.pressedSprite else type.idleSprite
        )
    }

    override fun event(context: EventContext, event: Event) = false

    internal fun triggerDown() {
        isPressed = true
    }

    internal fun triggerUp() {
        isPressed = false
    }
}