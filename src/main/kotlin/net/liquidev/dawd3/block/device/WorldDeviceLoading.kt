package net.liquidev.dawd3.block.device

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

/**
 * Logic that needs to run to rebuild the logical device graph upon world loading.
 */
object WorldDeviceLoading {
    private val pendingDevicesToRebuild = arrayListOf<BlockPos>()

    fun enqueueDeviceForRebuild(blockEntity: DeviceBlockEntity) {
        pendingDevicesToRebuild.add(blockEntity.pos)
    }

    private fun rebuildPendingDevices(world: ClientWorld) {
        for (blockPosition in pendingDevicesToRebuild) {
            val inputBlockEntity = world.getBlockEntity(blockPosition) as DeviceBlockEntity
            for ((inputPortName, inputConnection) in inputBlockEntity.inputConnections) {
                val outputBlockEntity = world.getBlockEntity(inputConnection.blockPosition)
                if (outputBlockEntity is DeviceBlockEntity) {
                    DeviceBlockEntity.connectLogicalDevices(
                        inputBlockEntity,
                        inputPortName,
                        outputBlockEntity,
                        inputConnection.outputPortName,
                        inputConnection.color
                    )
                    // Reconcile the physical connection, just in case it goes only to one side.
                    DeviceBlockEntity.connectPhysicalDevices(
                        inputBlockEntity,
                        inputPortName,
                        outputBlockEntity,
                        inputConnection.outputPortName,
                        inputConnection.color
                    )
                }
            }
        }
        pendingDevicesToRebuild.clear()
    }

    fun registerClientTickEvent() {
        ClientTickEvents.START_WORLD_TICK.register { clientWorld ->
            rebuildPendingDevices(clientWorld)
        }
    }
}