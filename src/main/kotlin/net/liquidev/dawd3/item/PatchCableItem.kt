package net.liquidev.dawd3.item

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.block.device.DeviceBlockInteractions
import net.liquidev.dawd3.events.PlayerEvents
import net.liquidev.dawd3.net.ConnectPorts
import net.liquidev.dawd3.net.StartConnectingPorts
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos

class PatchCableItem(settings: Settings, val color: Byte) : BasicItem(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (context.player?.isSneaking != true) {
            val blockEntity = context.world.getBlockEntity(context.blockPos)
            if (!context.world.isClient && blockEntity is DeviceBlockEntity) {
                val portName =
                    DeviceBlockInteractions.findUsedPort(context.hitResult, blockEntity.descriptor)
                if (portName != null) {
                    useOnPort(context, portName)
                }
            }
            return ActionResult.success(context.world.isClient)
        } else {
            return context.world.getBlockState(context.blockPos)
                .onUse(context.world, context.player, context.hand, context.hitResult)
        }
    }

    private fun useOnPort(context: ItemUsageContext, portName: PortName) {
        val player = context.player
        if (player == null || player !is ServerPlayerEntity) {
            return
        }

        val ongoingConnection = ongoingConnectionsServer[player]
        if (ongoingConnection == null) {
            startConnecting(player, OngoingConnection(context.blockPos, portName, color))
            ServerPlayNetworking.send(
                player,
                StartConnectingPorts.id,
                StartConnectingPorts(context.blockPos, portName.toString(), color).serialize()
            )
        } else {
            if (portName.direction != ongoingConnection.portName.direction) {
                val world = context.world as ServerWorld

                val fromPosition = ongoingConnection.blockPosition
                val fromPort = ongoingConnection.portName
                val toPosition = context.blockPos

                val witnesses = PlayerLookup.tracking(world, fromPosition).toHashSet()
                witnesses.addAll(PlayerLookup.tracking(world, toPosition))
                for (witness in witnesses) {
                    ServerPlayNetworking.send(
                        witness,
                        ConnectPorts.id,
                        ConnectPorts(
                            fromPosition,
                            fromPort.id.toString(),
                            toPosition,
                            portName.id.toString(),
                            ongoingConnection.color,
                        ).serialize()
                    )
                }

                val fromBlockEntity = world.getBlockEntity(fromPosition)
                val toBlockEntity = world.getBlockEntity(toPosition)
                if (fromBlockEntity !is DeviceBlockEntity || toBlockEntity !is DeviceBlockEntity) {
                    logger.error("from or to-block entity is no longer a device")
                    return
                }
                DeviceBlockEntity.connectPhysicalDevices(
                    fromBlockEntity,
                    fromPort,
                    toBlockEntity,
                    portName,
                    color,
                )

                clearOngoingConnection(player)
            }
        }
    }

    data class OngoingConnection(
        val blockPosition: BlockPos,
        val portName: PortName,
        val color: Byte,
    )

    companion object {
        private var logger = Mod.logger<PatchCableItem>()

        internal val ongoingConnectionsServer = hashMapOf<ServerPlayerEntity, OngoingConnection>()
        internal val ongoingConnectionsClient = hashMapOf<ClientPlayerEntity, OngoingConnection>()

        private fun clearOngoingConnection(player: PlayerEntity) {
            ongoingConnectionsServer.remove(player)
            ongoingConnectionsClient.remove(player)
        }

        internal fun removeAllConnectionsAtBlock(blockPosition: BlockPos) {
            ongoingConnectionsServer.values.removeAll { it.blockPosition == blockPosition }
            ongoingConnectionsClient.values.removeAll { it.blockPosition == blockPosition }
        }

        fun startConnecting(player: PlayerEntity, connection: OngoingConnection) {
            when (player) {
                is ServerPlayerEntity -> ongoingConnectionsServer[player] = connection
                is ClientPlayerEntity -> ongoingConnectionsClient[player] = connection
                else -> {
                    logger.warn("player of unexpected class found")
                }
            }
        }

        init {
            PlayerEvents.ITEM_SWITCHED.register { player ->
                clearOngoingConnection(player)
            }
        }
    }
}