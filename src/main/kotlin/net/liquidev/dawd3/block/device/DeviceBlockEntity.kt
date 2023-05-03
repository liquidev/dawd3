package net.liquidev.dawd3.block.device

import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
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
    val controls = descriptor.initControls()
    val controlMap = ControlMap(controls)

    internal data class InputConnection(
        val blockPosition: BlockPos,
        val outputPortName: OutputPortName,
        val color: Byte,
    )

    internal val inputConnections = hashMapOf<InputPortName, InputConnection>()

    /**
     * This field is mostly used for tracing back connections from outputs of this block to inputs
     * of another block, to know which blocks' connections to sever when this one's destroyed.
     */
    internal val outputConnections = hashMapOf<OutputPortName, BlockPos>()

    /** NBT compound keys. */
    private object Nbt {
        const val controls = "controls"

        const val inputConnections = "inputConnections"

        object InputConnection {
            const val input = "input"
            const val output = "output"
            const val position = "position"
            const val color = "color"
        }

        const val outputConnections = "outputConnections"

        object OutputConnection {
            const val port = "port"
            const val block = "block"
        }
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        val controlsNbt = nbt.getCompound(Nbt.controls)
        controls.visitControls { controlName, control ->
            val controlNameString = controlName.toString()
            if (controlNameString in controlsNbt) {
                val element = controlsNbt.get(controlNameString)
                if (element != null) {
                    control.valueFromNBT(element)
                }
            }
        }

        val inputConnectionsNbt =
            nbt.getList(Nbt.inputConnections, NbtElement.COMPOUND_TYPE.toInt())
        for (i in 0 until inputConnectionsNbt.size) {
            val connectionNbt = inputConnectionsNbt.getCompound(i)

            val outputString = connectionNbt.getString(Nbt.InputConnection.output)
            val output = PortName.fromString(outputString) ?: continue
            if (output !is OutputPortName) {
                logger.error("NBT declares 'inputConnections.output' field that refers to an input port")
                continue
            }

            val inputString = connectionNbt.getString(Nbt.InputConnection.input)
            val input = PortName.fromString(inputString) ?: continue
            if (input !is InputPortName) {
                logger.error("NBT declares 'inputConnections.input' field that refers to an output port")
                continue
            }

            val blockPosition =
                NbtHelper.toBlockPos(connectionNbt.getCompound(Nbt.InputConnection.position))
            val color = connectionNbt.getByte(Nbt.InputConnection.color)

            inputConnections[input] = InputConnection(blockPosition, outputPortName = output, color)
        }

        val outputConnectionsNbt =
            nbt.getList(Nbt.outputConnections, NbtElement.COMPOUND_TYPE.toInt())
        for (i in 0 until outputConnectionsNbt.size) {
            val connectionNbt = outputConnectionsNbt.getCompound(i)

            val portString = connectionNbt.getString(Nbt.OutputConnection.port)
            val port = PortName.fromString(portString) ?: continue
            if (port !is OutputPortName) {
                logger.error("NBT declares 'outputConnections.port' field that refers to an input port")
                continue
            }
            val blockPosition =
                NbtHelper.toBlockPos(connectionNbt.getCompound(Nbt.OutputConnection.block))

            outputConnections[port] = blockPosition
        }
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)

        val controlsNbt = NbtCompound()
        controls.visitControls { controlName, control ->
            controlsNbt.put(controlName.toString(), control.valueToNBT())
        }
        nbt.put(Nbt.controls, controlsNbt)

        val inputConnectionsNbt = NbtList()
        for ((inputPortName, connection) in inputConnections) {
            val connectionNbt = NbtCompound()
            connectionNbt.putString(
                Nbt.InputConnection.output,
                connection.outputPortName.toString()
            )
            connectionNbt.putString(Nbt.InputConnection.input, inputPortName.toString())
            connectionNbt.put(
                Nbt.InputConnection.position,
                NbtHelper.fromBlockPos(connection.blockPosition)
            )
            connectionNbt.putByte(Nbt.InputConnection.color, connection.color)
            inputConnectionsNbt.add(connectionNbt)
        }
        nbt.put(Nbt.inputConnections, inputConnectionsNbt)

        val outputConnectionsNbt = NbtList()
        for ((outputPortName, blockPosition) in outputConnections) {
            val connectionNbt = NbtCompound()
            connectionNbt.putString(Nbt.OutputConnection.port, outputPortName.toString())
            connectionNbt.put(Nbt.OutputConnection.block, NbtHelper.fromBlockPos(blockPosition))
            outputConnectionsNbt.add(connectionNbt)
        }
        nbt.put(Nbt.outputConnections, outputConnectionsNbt)
    }

    override fun onClientLoad(world: ClientWorld) {
        clientState = descriptor.onClientLoad(controls, world)
        WorldDeviceLoading.enqueueDeviceForRebuild(this)
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

    fun severConnectionsInPort(portName: PortName): Boolean {
        val world = world ?: return false

        val clientState = clientState
        var severedAnyConnections = false
        severedAnyConnections = severedAnyConnections or if (clientState != null) {
            val resolvedPortName =
                if (portName is OutputPortName) portName.resolveInstance() else portName
            Devices.severAllConnectionsInPort(clientState.logicalDevice, resolvedPortName) > 0
        } else {
            false
        }

        when (portName) {
            is InputPortName -> {
                val inputConnection = inputConnections.remove(portName)
                if (inputConnection != null) {
                    val blockEntity = world.getBlockEntity(inputConnection.blockPosition)
                    if (blockEntity is DeviceBlockEntity) {
                        blockEntity.outputConnections.remove(inputConnection.outputPortName)
                    }
                    severedAnyConnections = true
                }
            }
            is OutputPortName -> {
                val blockPosition = outputConnections.remove(portName)
                if (blockPosition != null) {
                    val blockEntity = world.getBlockEntity(blockPosition)
                    if (blockEntity is DeviceBlockEntity) {
                        blockEntity.inputConnections.values.removeAll { it.outputPortName == portName }
                    }
                    severedAnyConnections = true
                }
            }
        }
        return severedAnyConnections
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
            outputBlockEntity.outputConnections[outputPort] = inputBlockEntity.pos
            try {
                Devices.makeConnection(
                    outputDevice,
                    outputPort.resolveInstance(),
                    inputDevice,
                    inputPort
                )
            } catch (noSuchPort: NoSuchPortException) {
                logger.error("NoSuchPortException caught: $noSuchPort")
            }
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
            outputBlockEntity.outputConnections[outputPort] = inputBlockEntity.pos
            outputBlockEntity.markDirty()
        }
    }
}