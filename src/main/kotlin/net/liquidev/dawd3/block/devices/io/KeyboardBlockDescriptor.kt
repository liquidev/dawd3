package net.liquidev.dawd3.block.devices.io

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.io.KeyboardDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.ui.widget.Window
import net.liquidev.dawd3.ui.widget.keyboard.Keyboard
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object KeyboardBlockDescriptor : DeviceBlockDescriptor<KeyboardBlockDescriptor.ClientState, KeyboardDevice.Controls> {
    override val id = Identifier(Mod.id, "keyboard")

    override val portLayout = PhysicalPort.layout {
        port(KeyboardDevice.pitchOutput, position = Vec2f(0.25f, 0.25f), PhysicalPort.Side.Front)
        port(KeyboardDevice.triggerOutput, position = Vec2f(0.75f, 0.25f), PhysicalPort.Side.Front)
        port(KeyboardDevice.velocityOutput, position = Vec2f(0.5f, 0.75f), PhysicalPort.Side.Front)
    }

    class ClientState(controls: KeyboardDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(KeyboardDevice(), controls)
    }

    override fun initControls() = KeyboardDevice.Controls()

    override fun onClientLoad(controls: KeyboardDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<KeyboardDevice.Controls> {
        override fun open(controls: KeyboardDevice.Controls, x: Float, y: Float) =
            Window(
                x, y,
                width = 332f,
                height = 56f,
                title = Text.translatable("block.dawd3.keyboard")
            ).apply {
                children.add(Keyboard(x = 8f, y = 16f, firstNote = -33, lastNote = 27, controls))
            }
    }
}