package net.liquidev.dawd3.ui.widget.keyboard

import net.liquidev.dawd3.audio.device.FloatControl
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
    val pitchControl: FloatControl,
) : Widget(x, y) {

    enum class Type(
        val width: Int,
        val height: Int,
        val idleSprite: Sprite,
        val pressedSprite: Sprite,
        val currentPitchSprite: Sprite,
    ) {
        White(
            width = 10,
            height = 32,
            idleSprite = Sprite(u = 32, v = 16, width = 10, height = 32),
            pressedSprite = Sprite(u = 42, v = 16, width = 10, height = 32),
            currentPitchSprite = Sprite(u = 59, v = 16, width = 4, height = 4),
        ),
        Black(
            width = 7,
            height = 20,
            idleSprite = Sprite(u = 52, v = 16, width = 7, height = 20),
            pressedSprite = Sprite(u = 52, v = 36, width = 7, height = 20),
            currentPitchSprite = Sprite(u = 59, v = 20, width = 3, height = 3),
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
        if (pitchControl.value == note) {
            Render.sprite(
                matrices,
                x = (width / 2f - type.currentPitchSprite.width / 2f).toInt(),
                y = height - type.currentPitchSprite.height - 2,
                Rack.atlas,
                type.currentPitchSprite
            )
        }
    }

    override fun event(context: EventContext, event: Event) = false

    internal fun triggerDown() {
        isPressed = true
    }

    internal fun triggerUp() {
        isPressed = false
    }
}