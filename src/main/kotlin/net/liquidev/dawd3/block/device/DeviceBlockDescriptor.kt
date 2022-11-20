package net.liquidev.dawd3.block.device

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Material
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier

typealias AnyDeviceBlockDescriptor = DeviceBlockDescriptor<DeviceBlockDescriptor.ClientState, Any>

interface DeviceBlockDescriptor<out CS : DeviceBlockDescriptor.ClientState, out ServerState> {
    val id: Identifier
    val blockSettings: AbstractBlock.Settings
        // The default block settings are used for metal-enclosed modules.
        get() = FabricBlockSettings
            .of(Material.METAL)
            .hardness(5.0f)
            .resistance(6.0f)

    val portLayout: Array<PhysicalPort>

    fun onClientLoad(world: ClientWorld): CS
    fun onClientUnload(state: @UnsafeVariance CS, world: ClientWorld) {}

    interface ClientState {
        val logicalDevice: DeviceInstance
    }
}

