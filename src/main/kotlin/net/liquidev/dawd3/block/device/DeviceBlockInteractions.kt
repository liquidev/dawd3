package net.liquidev.dawd3.block.device

import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.common.*
import net.liquidev.dawd3.datagen.device.DeviceBlockModel
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

object DeviceBlockInteractions {
    fun findUsedPort(
        hit: BlockHitResult,
        descriptor: AnyDeviceBlockDescriptor,
    ): PortName? {
        val horizontalSide = HorizontalDirection.fromDirection(hit.side) ?: return null
        val usePosition =
            calculateUsePositionOnHorizontalFace(hit.pos, hit.blockPos, horizontalSide)
        for ((portName, port) in descriptor.portLayout) {
            if (isUsedOnPort(port, usePosition)) {
                return portName
            }
        }
        return null
    }

    private fun calculateUsePositionOnHorizontalFace(
        hitPosition: Vec3d,
        blockPosition: BlockPos,
        side: HorizontalDirection,
    ): Vec2f {
        val relativeHitPosition = (hitPosition - blockPosition.toVec3d()).toVec3f()
        val faceCorrection = side.faceCorrection
        return faceCorrection * (side.direction.to2DPlane * relativeHitPosition)
    }

    private fun isUsedOnPort(
        port: PhysicalPort,
        usePosition: Vec2f,
    ): Boolean {
        val usePositionInPixels = usePosition * Vec2f(16f, 16f)
        val portTopLeft = DeviceBlockModel.relativeToAbsolutePortPosition(port.position)
        val portBottomRight = portTopLeft + DeviceBlockModel.portSize
        return pointInRectangle(usePositionInPixels, portTopLeft, portBottomRight)
    }
}