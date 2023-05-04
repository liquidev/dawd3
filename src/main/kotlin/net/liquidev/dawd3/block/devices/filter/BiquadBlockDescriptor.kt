package net.liquidev.dawd3.block.devices.filter

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.filter.BiquadDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.ui.units.PercentageValue
import net.liquidev.dawd3.ui.units.SiValue
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

class BiquadBlockDescriptor(
    override val id: Identifier,
    private val type: BiquadDevice.Type,
) : DeviceBlockDescriptor<BiquadBlockDescriptor.ClientState, BiquadDevice.Controls> {
    companion object {
        val lowPass =
            BiquadBlockDescriptor(Identifier(Mod.id, "biquad_low_pass"), BiquadDevice.Type.LowPass)
        val highPass = BiquadBlockDescriptor(
            Identifier(Mod.id, "biquad_high_pass"),
            BiquadDevice.Type.HighPass
        )
        val bandPass = BiquadBlockDescriptor(
            Identifier(Mod.id, "biquad_band_pass"),
            BiquadDevice.Type.BandPass
        )
        val notch =
            BiquadBlockDescriptor(Identifier(Mod.id, "biquad_notch"), BiquadDevice.Type.Notch)
    }

    override val portLayout = PhysicalPort.layout {
        port(BiquadDevice.inputPort, position = Vec2f(0.25f, 0.25f), PhysicalPort.Side.Front)
        port(BiquadDevice.frequencyCVPort, position = Vec2f(0.75f, 0.25f), PhysicalPort.Side.Front)
        port(BiquadDevice.resonanceCVPort, position = Vec2f(0.25f, 0.75f), PhysicalPort.Side.Front)
        port(BiquadDevice.outputPort, position = Vec2f(0.75f, 0.75f), PhysicalPort.Side.Front)
    }

    class ClientState(type: BiquadDevice.Type, controls: BiquadDevice.Controls) :
        DeviceBlockDescriptor.ClientState {
        override val logicalDevice =
            DeviceInstance.create(BiquadDevice(type), controls)
    }

    override fun initControls() = BiquadDevice.Controls()

    override fun onClientLoad(controls: BiquadDevice.Controls, world: ClientWorld) =
        ClientState(type, controls)

    override val ui = object : DeviceBlockDescriptor.UI<BiquadDevice.Controls> {
        override fun open(controls: BiquadDevice.Controls, x: Float, y: Float) =
            Window(
                x,
                y,
                width = 121f,
                height = 48f,
                Text.translatable(id.toTranslationKey("block"))
            ).apply {
                children.add(
                    Knob(
                        x = 8f, y = 18f,
                        controls.frequency,
                        min = 1f, max = 25000f,
                        Knob.Color.Orange,
                        unit = SiValue.frequency,
                    )
                )
                children.add(
                    Knob(
                        x = 36f, y = 18f,
                        controls.frequencyCV,
                        min = -20000f, max = 20000f,
                        Knob.Color.Orange,
                        unit = SiValue.frequency,
                    )
                )
                children.add(
                    Knob(
                        x = 64f, y = 18f,
                        controls.resonance,
                        min = 0f, max = 1f,
                        Knob.Color.Blue,
                        unit = PercentageValue
                    )
                )
                children.add(
                    Knob(
                        x = 92f, y = 18f,
                        controls.resonanceCV,
                        min = -1f, max = 1f,
                        Knob.Color.Blue,
                        unit = PercentageValue,
                    )
                )
            }
    }
}