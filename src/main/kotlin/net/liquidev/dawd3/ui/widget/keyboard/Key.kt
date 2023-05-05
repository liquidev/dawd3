package net.liquidev.dawd3.ui.widget.keyboard

import net.liquidev.dawd3.audio.device.FloatControl
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.render.Sprite
import net.liquidev.dawd3.ui.DeviceEventContext
import net.liquidev.dawd3.ui.Event
import net.liquidev.dawd3.ui.Message
import net.liquidev.dawd3.ui.RackScreen
import net.liquidev.dawd3.ui.widget.DeviceWidget
import net.minecraft.client.util.math.MatrixStack

class Key(
    x: Float,
    y: Float,
    val type: Type,
    val note: Float,
    val pitchControl: FloatControl,
) : DeviceWidget(x, y) {

    enum class Type(
        val width: Float,
        val height: Float,
        val idleSprite: Sprite,
        val pressedSprite: Sprite,
        val currentPitchSprite: Sprite,
    ) {
        White(
            width = 10f,
            height = 32f,
            idleSprite = Sprite(u = 32f, v = 16f, width = 10f, height = 32f),
            pressedSprite = Sprite(u = 42f, v = 16f, width = 10f, height = 32f),
            currentPitchSprite = Sprite(u = 59f, v = 16f, width = 4f, height = 4f),
        ),
        Black(
            width = 7f,
            height = 20f,
            idleSprite = Sprite(u = 52f, v = 16f, width = 7f, height = 20f),
            pressedSprite = Sprite(u = 52f, v = 36f, width = 7f, height = 20f),
            currentPitchSprite = Sprite(u = 59f, v = 20f, width = 3f, height = 3f),
        ),
    }

    override val width = type.width
    override val height = type.height

    private var isPressed = false

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        Render.sprite(
            matrices,
            x = 0f,
            y = 0f,
            RackScreen.atlas,
            if (isPressed) type.pressedSprite else type.idleSprite
        )
        if (pitchControl.value == note) {
            Render.sprite(
                matrices,
                x = width / 2f - type.currentPitchSprite.width / 2f,
                y = height - type.currentPitchSprite.height - 2,
                RackScreen.atlas,
                type.currentPitchSprite
            )
        }
    }

    override fun event(context: DeviceEventContext, event: Event) = Message.eventIgnored

    internal fun triggerDown() {
        isPressed = true
    }

    internal fun triggerUp() {
        isPressed = false
    }
}