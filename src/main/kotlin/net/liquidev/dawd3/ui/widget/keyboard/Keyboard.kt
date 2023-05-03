package net.liquidev.dawd3.ui.widget.keyboard

import net.liquidev.dawd3.audio.device.BlockEntityControls
import net.liquidev.dawd3.audio.devices.io.KeyboardDevice
import net.liquidev.dawd3.ui.*
import net.liquidev.dawd3.ui.widget.Container
import org.lwjgl.glfw.GLFW

class Keyboard(
    x: Int,
    y: Int,
    firstNote: Int,
    lastNote: Int,
    private val controls: KeyboardDevice.Controls,
) : Container(x, y) {

    override val children = run {
        var whiteX = 0
        List(lastNote - firstNote) { index ->
            val note = firstNote + index
            val keyType = octave[note.mod(octave.size)]
            val keyX = when (keyType) {
                Key.Type.White -> {
                    val xx = whiteX
                    whiteX += keyType.width - 1
                    xx
                }
                Key.Type.Black -> whiteX - keyType.width / 2 - 1
            }
            Key(keyX, y = 0, keyType, note.toFloat(), controls)
            // We want black keys to render after white keys.
        }.sortedBy { it.type }
    }

    override val width = children.maxOf { it.x + it.width }
    override val height = children.maxOf { it.y + it.height }

    private var pressedKey: Key? = null

    override fun event(context: EventContext, event: Event): Boolean {
        if (event is MouseButton && event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            mouseIsDown = event.action == Action.Down && containsRelativePoint(
                event.mouseX.toInt(),
                event.mouseY.toInt()
            )
            if (mouseIsDown) {
                setPressedKey(context, findPressedKey(event.mouseX.toInt(), event.mouseY.toInt()))
            } else {
                setPressedKey(context, null)
            }
            return mouseIsDown && pressedKey != null
        }

        if (event is MouseMove && mouseIsDown) {
            setPressedKey(context, findPressedKey(event.mouseX.toInt(), event.mouseY.toInt()))
        }

        return false
    }

    private fun findPressedKey(mouseX: Int, mouseY: Int): Key? =
        children.findLast { it.containsRelativePoint(mouseX - it.x, mouseY - it.y) }

    private fun setPressedKey(context: EventContext, newKey: Key?) {
        val previousKey = pressedKey
        pressedKey = newKey
        if (newKey != previousKey) {
            previousKey?.triggerUp()
            if (newKey != null) {
                newKey.triggerDown()
                BlockEntityControls.setFloatControlValue(
                    context.blockPosition,
                    controls.pitch,
                    newKey.note
                )
                BlockEntityControls.setFloatControlValue(
                    context.blockPosition,
                    controls.trigger,
                    1f
                )
                // TODO: Key velocity.
                BlockEntityControls.setFloatControlValue(
                    context.blockPosition,
                    controls.velocity,
                    1f
                )
            } else {
                BlockEntityControls.setFloatControlValue(
                    context.blockPosition,
                    controls.trigger,
                    0f
                )
                BlockEntityControls.setFloatControlValue(
                    context.blockPosition,
                    controls.velocity,
                    0f
                )
            }
        }
    }

    companion object {
        var mouseIsDown = false

        val octave = arrayOf(
            // For efficiency we start with A, since pitch signals represent it as 0.
            Key.Type.White, // A
            Key.Type.Black, // A#
            Key.Type.White, // B
            Key.Type.White, // C
            Key.Type.Black, // C#
            Key.Type.White, // D
            Key.Type.Black, // D#
            Key.Type.White, // E
            Key.Type.White, // F
            Key.Type.Black, // F#
            Key.Type.White, // G
            Key.Type.Black, // G#
        )
    }
}