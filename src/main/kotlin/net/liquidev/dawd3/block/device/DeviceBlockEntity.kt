package net.liquidev.dawd3.block.device

import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.Devices
import net.liquidev.dawd3.audio.device.InputPortName
import net.liquidev.dawd3.audio.device.OutputPortName
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.block.entity.D3BlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.world.ClientWorld
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos

private typealias DeviceBlockFactory = FabricBlockEntityTypeBuilder.Factory<DeviceBlockEntity>

class DeviceBlockEntity(
    type: BlockEntityType<DeviceBlockEntity>,
    blockPos: BlockPos,
    blockState: BlockState,
    val descriptor: AnyDeviceBlockDescriptor,
) : D3BlockEntity(type, blockPos, blockState) {

    private var clientState: DeviceBlockDescriptor.ClientState? = null
    private var serverState: Any? = null

    internal data class InputConnection(
        val blockPosition: BlockPos,
        val outputPortName: OutputPortName,
        val color: Byte,
    )

    internal val inputConnections = hashMapOf<InputPortName, InputConnection>()

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        val nbtConnections = nbt.getList("connections", NbtElement.COMPOUND_TYPE.toInt())

        for (i in 0 until nbtConnections.size) {
            val connectionNbt = nbtConnections.getCompound(i)

            val outputString = connectionNbt.getString("output")
            val output = PortName.fromString(outputString) ?: continue
            if (output !is OutputPortName) {
                logger.error("NBT declares 'output' field that refers to an input port")
                continue
            }

            val inputString = connectionNbt.getString("input")
            val input = PortName.fromString(inputString) ?: continue
            if (input !is InputPortName) {
                logger.error("NBT declares 'input' field that refers to an output port")
                continue
            }

            val blockPosition = NbtHelper.toBlockPos(connectionNbt.getCompound("position"))
            val color = connectionNbt.getByte("color")

            inputConnections[input] = InputConnection(blockPosition, outputPortName = output, color)
        }
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)

        val connectionsNbt = NbtList()
        for ((inputPortName, connection) in inputConnections) {
            val connectionNbt = NbtCompound()
            connectionNbt.putString("output", connection.outputPortName.toString())
            connectionNbt.putString("input", inputPortName.toString())
            connectionNbt.put("position", NbtHelper.fromBlockPos(connection.blockPosition))
            connectionNbt.putByte("color", connection.color)
            connectionsNbt.add(connectionNbt)
        }
        nbt.put("connections", connectionsNbt)
    }

    override fun onClientLoad(world: ClientWorld) {
        clientState = descriptor.onClientLoad(world)
    }

    override fun onClientUnload(world: ClientWorld) {
        val clientState = clientState
        if (clientState != null) {
            descriptor.onClientUnload(clientState, world)
            Devices.severAllConnectionsInDevice(clientState.logicalDevice)
        }
    }

    internal fun reapInvalidConnections() {
        inputConnections.keys.removeAll {
            descriptor.portLayout[it] == null
        }
        val world = world
        if (world != null) {
            inputConnections.values.removeAll {
                world.getBlockEntity(it.blockPosition) !is DeviceBlockEntity
            }
        }
    }

    companion object {
        val logger = Mod.logger<DeviceBlockEntity>()

        fun factory(descriptor: AnyDeviceBlockDescriptor): DeviceBlockFactory =
            DeviceBlockFactory { blockPos, blockState ->
                val type by lazy { Blocks.deviceBlocks[descriptor.id]!!.blockEntity }
                DeviceBlockEntity(type, blockPos, blockState, descriptor)
            }

        fun connectLogicalDevices(
            fromBlockEntity: DeviceBlockEntity,
            fromPort: PortName,
            toBlockEntity: DeviceBlockEntity,
            toPort: PortName,
            cableColor: Byte,
        ) {
            val (outputBlockEntity, outputPort, inputBlockEntity, inputPort) = Devices.sortPortsByInputAndOutput(
                fromBlockEntity,
                fromPort,
                toBlockEntity,
                toPort
            ) ?: throw PortDirectionException("connected ports must be of opposing directions")
            val outputDevice = outputBlockEntity.clientState?.logicalDevice ?: return
            val inputDevice = inputBlockEntity.clientState?.logicalDevice ?: return
            inputBlockEntity.inputConnections[inputPort] =
                InputConnection(outputBlockEntity.pos, outputPort, cableColor)
            Devices.makeConnection(
                outputDevice,
                outputPort.resolveInstance(),
                inputDevice,
                inputPort
            )
        }

        fun connectPhysicalDevices(
            fromBlockEntity: DeviceBlockEntity,
            fromPort: PortName,
            toBlockEntity: DeviceBlockEntity,
            toPort: PortName,
            cableColor: Byte,
        ) {
            val (outputBlockEntity, outputPort, inputBlockEntity, inputPort) = Devices.sortPortsByInputAndOutput(
                fromBlockEntity,
                fromPort,
                toBlockEntity,
                toPort,
            ) ?: throw PortDirectionException("connected ports must be of opposing directions")
            inputBlockEntity.inputConnections[inputPort] =
                InputConnection(outputBlockEntity.pos, outputPort, cableColor)
            outputBlockEntity.markDirty()
        }
    }
}