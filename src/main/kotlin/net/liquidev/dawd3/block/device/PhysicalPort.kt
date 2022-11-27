package net.liquidev.dawd3.block.device

import net.liquidev.dawd3.audio.device.OutputPortName
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.common.Affine2x2f
import net.liquidev.dawd3.common.HorizontalDirection
import net.liquidev.dawd3.common.div
import net.liquidev.dawd3.common.plus
import net.liquidev.dawd3.datagen.device.DeviceBlockModel
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

/** The physical appearance of a port. */
data class PhysicalPort(
    /** The coordinates are relative, in range [0.0, 1.0] where 0 is top-left and 1 is bottom-right. */
    val position: Vec2f,
    val side: Side,
    val logicalPort: PortName,
) {
    /** Where to place the port on the model. */
    enum class Side(
        /** The transform matrix to apply to ports' coordinates in the model. */
        val modelTransform: Affine2x2f,
        /** The model face on which the port is placed. */
        val face: HorizontalDirection,
    ) {
        Front(
            modelTransform = Affine2x2f(1f, 0f, 0f, 1f),
            face = HorizontalDirection.North,
        ),
        Back(
            modelTransform = Affine2x2f(-1f, 0f, 0f, -1f, translateX = 16f, translateY = 16f),
            face = HorizontalDirection.South,
        ),
    }

    /** Where the port's patch cable cord is located relative to a block's center. */
    fun blockCablePosition(
        blockFacing: HorizontalDirection,
        protrusionAmount: Float,
    ): Vec3f {
        val facing = (blockFacing + side.face).clockwise()
        val absolutePosition =
            DeviceBlockModel.relativeToAbsolutePortPosition(position) + DeviceBlockModel.portSize / 2f
        val invertedPosition = Vec2f(1f - absolutePosition.x / 16f, 1f - absolutePosition.y / 16f)
        val forward = Vec3f(protrusionAmount, invertedPosition.y - 0.5f, invertedPosition.x - 0.5f)
        return facing.rotateY(forward) + Vec3f(0.5f, 0.5f, 0.5f)
    }

    companion object {
        class LayoutBuilder {
            internal val layout = PhysicalPortLayout()

            fun port(portName: PortName, physicalPort: PhysicalPort) {
                layout[portName] = physicalPort
            }

            fun port(portName: PortName, position: Vec2f, side: Side) {
                port(portName, PhysicalPort(position, side, portName))
            }

            /**
             * Can be used to create physical ports whose name is different from the logical
             * port name, to allow for aliasing multiple physical ports onto a single logical port.
             *
             * Note that this is only allowed for output ports, because input ports cannot have
             * multiple signals going into them.
             */
            fun port(portName: OutputPortName, instanceName: String, position: Vec2f, side: Side) {
                port(portName.makeInstanced(instanceName), PhysicalPort(position, side, portName))
            }
        }

        fun layout(build: LayoutBuilder.() -> Unit): PhysicalPortLayout {
            val builder = LayoutBuilder()
            build(builder)
            return builder.layout
        }
    }
}

typealias PhysicalPortLayout = HashMap<PortName, PhysicalPort>
