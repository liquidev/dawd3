package net.liquidev.dawd3.block.device

import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.common.Affine2x2
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f

/** The physical appearance of a port. */
data class PhysicalPort(
    val port: PortName,
    /** The coordinates are relative, in range [0.0, 1.0] where 0 is top-left and 1 is bottom-right. */
    val position: Vec2f,
    val side: Side,
) {
    /** Where to place the port on the model. */
    enum class Side(
        /** The transform matrix to apply to ports' coordinates in the model. */
        val transform: Affine2x2,
        /** The model face on which the port is placed. */
        val face: Direction,
    ) {
        Front(
            transform = Affine2x2(1f, 0f, 0f, 1f),
            face = Direction.NORTH,
        ),
        Back(
            transform = Affine2x2(-1f, 0f, 0f, -1f, translateX = 16f, translateY = 16f),
            face = Direction.SOUTH,
        ),
    }
}
