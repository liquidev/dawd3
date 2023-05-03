package net.liquidev.dawd3.block.device

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.common.*
import net.liquidev.dawd3.datagen.device.DeviceBlockModel
import net.liquidev.dawd3.item.PatchCableItem
import net.minecraft.block.BlockState
import net.minecraft.client.render.*
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.world.World
import java.util.function.Function
import kotlin.math.abs
import kotlin.math.floor

class DeviceBlockEntityRenderer(context: BlockEntityRendererFactory.Context) : BlockEntityRenderer<DeviceBlockEntity> {
    private companion object {
        val logger = Mod.logger<DeviceBlockEntityRenderer>()

        val renderLayer: RenderLayer = RenderLayer.of(
            "solid",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
            VertexFormat.DrawMode.TRIANGLE_STRIP,
            2048,
            MultiPhaseParameters.builder()
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .shader(RenderPhase.SOLID_SHADER)
                .texture(RenderPhase.BLOCK_ATLAS_TEXTURE)
                .cull(RenderPhase.DISABLE_CULLING)
                .build(true)
        )
        val renderLayerFactory = Function<Identifier, RenderLayer> { renderLayer }

        const val cableProtrusionAmount = 0.69f // nice
        const val cableThickness = 0.03f
        const val cableSegmentCount = 6
        const val cableSag = 0.2f
        val cableColorsSprite = SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            Identifier(Mod.id, "device/cable")
        )
    }

    private val blockRenderer = context.renderManager
    private val patchCablePlug = Blocks.patchCablePlug.defaultState

    override fun rendersOutsideBoundingBox(blockEntity: DeviceBlockEntity): Boolean {
        // TODO: This needs to return true if the block entity has cables attached.
        return true
    }

    override fun render(
        blockEntity: DeviceBlockEntity,
        tickDelta: Float,
        matrixStack: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
    ) {
        val world = blockEntity.world ?: return
        val blockState = world.getBlockState(blockEntity.pos)
        if (blockState.block !is DeviceBlock) {
            // render() gets called for a frame while the block is still air after it's destroyed.
            return
        }

        renderPlugsInsidePorts(
            world,
            blockState,
            blockEntity,
            matrixStack,
            vertexConsumers,
            overlay
        )
        renderCables(world, blockState, blockEntity, matrixStack, vertexConsumers, tickDelta)
    }

    private fun rotateToFaceFront(
        blockState: BlockState,
        matrices: MatrixStack,
    ) {
        // Choose center as the transform origin. That way we can rotate by kπ/2 to get all the
        // possible horizontal orientations of the block.
        matrices.translate(0.5, 0.0, 0.5)
        val facing = HorizontalDirection.fromDirection(blockState[Properties.HORIZONTAL_FACING])!!
        matrices.multiply(Quaternion.fromEulerXyz(0f, -facing.angle, 0f))
    }

    private fun renderPlugsInsidePorts(
        world: World,
        blockState: BlockState,
        blockEntity: DeviceBlockEntity,
        matrixStack: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        overlay: Int,
    ) {
        val facing = HorizontalDirection.fromDirection(blockState[Properties.HORIZONTAL_FACING])!!

        matrixStack.push()
        rotateToFaceFront(blockState, matrixStack)
        for ((_, connection) in PatchCableItem.ongoingConnectionsClient) {
            if (connection.blockPosition == blockEntity.pos) {
                val port = blockEntity.descriptor.portLayout[connection.portName]
                if (port != null) {
                    renderPlugInsidePort(
                        world, blockEntity, facing, port, matrixStack, vertexConsumers, overlay
                    )
                } else {
                    logger.error("ongoing connection references port name '${connection.portName.id}' which does not exist on the block entity")
                }
            }
        }

        // TODO: There's probably a better place to call this.
        //  Maybe we should actually not call this at all here?
        //  I'm worried that for a frame a device could be something other than DeviceBlockEntity
        //  and we'll get plugs without cables rendering permanently. Which would be bad.
        blockEntity.reapInvalidConnections()

        for ((portName, _) in blockEntity.inputConnections) {
            val inputPort = blockEntity.descriptor.portLayout[portName]!!
            renderPlugInsidePort(
                world, blockEntity, facing, inputPort, matrixStack, vertexConsumers, overlay
            )

        }
        matrixStack.pop()

        for ((_, connection) in blockEntity.inputConnections) {
            val outputBlockEntity =
                world.getBlockEntity(connection.blockPosition)
            if (outputBlockEntity is DeviceBlockEntity) {
                val outputBlockState = world.getBlockState(connection.blockPosition)
                val outputPort = outputBlockEntity.descriptor.portLayout[connection.outputPortName]
                if (outputPort != null) {
                    val delta = (outputBlockEntity.pos - blockEntity.pos).toVec3d()
                    matrixStack.push()
                    matrixStack.translate(delta.x, delta.y, delta.z)
                    rotateToFaceFront(outputBlockState, matrixStack)
                    renderPlugInsidePort(
                        world,
                        outputBlockEntity,
                        facing,
                        outputPort,
                        matrixStack,
                        vertexConsumers,
                        overlay
                    )
                    matrixStack.pop()
                }
            }
        }
    }

    private fun renderCables(
        world: World,
        blockState: BlockState,
        blockEntity: DeviceBlockEntity,
        matrixStack: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        tickDelta: Float,
    ) {
        val facing = HorizontalDirection.fromDirection(blockState[Properties.HORIZONTAL_FACING])!!

        for ((player, connection) in PatchCableItem.ongoingConnectionsClient) {
            if (connection.blockPosition == blockEntity.pos) {
                val port = blockEntity.descriptor.portLayout[connection.portName]
                if (port != null) {
                    val worldFrom = blockEntity.pos.toVec3d()
                    val forward = player.getRotationVec(tickDelta)
                    val playerPosition =
                        player.getCameraPosVec(tickDelta) + forward + Vec3d(0.0, -0.1, 0.0)
                    val delta = (playerPosition - worldFrom).toVec3f()
                    val start = port.blockCablePosition(facing, cableProtrusionAmount)
                    renderCable(
                        world,
                        matrixStack,
                        vertexConsumers,
                        worldFrom,
                        start,
                        delta,
                        connection.color.toFloat(),
                    )
                }
            }
        }

        for ((inputPortName, connection) in blockEntity.inputConnections) {
            val startAbsolute = blockEntity.pos.toVec3d()
            val endAbsolute = connection.blockPosition.toVec3d()

            // TODO: Null checks here?
            val inputPort = blockEntity.descriptor.portLayout[inputPortName]!!
            val outputBlockState = world.getBlockState(connection.blockPosition)
            // TODO: The line below will crash if the block is destroyed (becomes air)
            //  Though it never crashed for me during testing?
            val outputBlockFacing =
                HorizontalDirection.fromDirection(outputBlockState[Properties.HORIZONTAL_FACING])!!
            val outputBlockEntity =
                world.getBlockEntity(connection.blockPosition) as DeviceBlockEntity
            val outputPort = outputBlockEntity.descriptor.portLayout[connection.outputPortName]

            // The output port can be null if we find a port that doesn't exist on this device.
            if (outputPort != null) {
                val start = inputPort.blockCablePosition(facing, cableProtrusionAmount)
                val end = outputPort.blockCablePosition(outputBlockFacing, cableProtrusionAmount)
                val delta = (endAbsolute - startAbsolute).toVec3f() + end

                renderCable(
                    world,
                    matrixStack,
                    vertexConsumers,
                    worldFrom = blockEntity.pos.toVec3d(),
                    start,
                    delta,
                    connection.color.toFloat()
                )
            }
        }
    }

    private fun getFacingInWorldSpace(
        blockFacing: HorizontalDirection,
        portFacing: HorizontalDirection,
    ) =
        blockFacing + portFacing - HorizontalDirection.North

    private fun renderPlugInsidePort(
        world: World,
        blockEntity: DeviceBlockEntity,
        blockFacing: HorizontalDirection,
        port: PhysicalPort,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        overlay: Int,
    ) {
        val positionOnSide =
            (DeviceBlockModel.relativeToAbsolutePortPosition(port.position) + DeviceBlockModel.portSize / 2f) / 16f
        val face = port.side.face
        matrices.push()
        matrices.multiply(
            Quaternion.fromEulerXyz(0f, face.angle, 0f)
        )
        matrices.translate(1f - positionOnSide.x.toDouble(), 1f - positionOnSide.y.toDouble(), 0.0)

        val facing = getFacingInWorldSpace(blockFacing, face)
        val light =
            WorldRenderer.getLightmapCoordinates(world, blockEntity.pos.add(facing.vector))
        blockRenderer.renderBlockAsEntity(
            patchCablePlug, matrices, vertexConsumers, light, overlay
        )

        matrices.pop()
    }

    private fun catenary(sag: Float, t: Float): Float {
        // Not an actual catenary, but close enough.
        val u = 2f * t - 1f
        return sag * (u * u - 1f)
    }

    private fun renderCable(
        world: World,
        matrixStack: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        worldFrom: Vec3d,
        from: Vec3f,
        to: Vec3f,
        colorIndex: Float,
    ) {
        val forward = to - from
        val right = forward.copy()
        right.cross(Vec3f.POSITIVE_Y)
        right.normalize()
        right.multiplyComponentwise(cableThickness, cableThickness, cableThickness)
        val up = right.copy()
        up.cross(forward)
        up.normalize()
        up.multiplyComponentwise(cableThickness, cableThickness, cableThickness)
        renderCableWithThicknessVector(
            world,
            matrixStack,
            vertexConsumers,
            worldFrom,
            from,
            to,
            thicknessVector = up,
            colorIndex
        )
        renderCableWithThicknessVector(
            world,
            matrixStack,
            vertexConsumers,
            worldFrom,
            from,
            to,
            thicknessVector = right,
            colorIndex
        )
    }

    private fun renderCableWithThicknessVector(
        world: World,
        matrixStack: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        worldFrom: Vec3d,
        from: Vec3f,
        to: Vec3f,
        thicknessVector: Vec3f,
        colorIndex: Float,
    ) {
        val vertexBuffer = cableColorsSprite.getVertexConsumer(
            vertexConsumers,
            renderLayerFactory
        ) as SpriteTexturedVertexConsumer
        val matrices = matrixStack.peek()
        // ↑ Cast needed due to different interface method resolution rules in Kotlin.
        // See comment in addCableSegment.

        val uLeft = colorIndex / 16f
        val uRight = (colorIndex + 1) / 16f

        addCableSegment(
            world,
            matrices,
            vertexBuffer,
            worldFrom.x,
            worldFrom.y,
            worldFrom.z,
            from.x,
            from.y,
            from.z,
            thicknessVector,
            uLeft,
            uRight,
            v = 0f,
        )

        val sag = cableSag + abs(to.y - from.y) * 0.2f
        for (i in 1 until cableSegmentCount) {
            val t = i.toFloat() / cableSegmentCount.toFloat()
            val sagOffset = catenary(sag, t)
            val x = lerp(from.x, to.x, t)
            val y = lerp(from.y, to.y, t) + sagOffset
            val z = lerp(from.z, to.z, t)
            addCableSegment(
                world,
                matrices,
                vertexBuffer,
                worldFrom.x,
                worldFrom.y,
                worldFrom.z,
                x,
                y,
                z,
                thicknessVector,
                uLeft,
                uRight,
                v = t,
            )
        }

        addCableSegment(
            world,
            matrices,
            vertexBuffer,
            worldFrom.x,
            worldFrom.y,
            worldFrom.z,
            to.x,
            to.y,
            to.z,
            thicknessVector,
            uLeft,
            uRight,
            v = 1f,
        )
    }

    private fun addCableSegment(
        world: World,
        matrices: MatrixStack.Entry,
        vertexBuffer: SpriteTexturedVertexConsumer,
        originX: Double,
        originY: Double,
        originZ: Double,
        x: Float,
        y: Float,
        z: Float,
        thickness: Vec3f,
        uLeft: Float,
        uRight: Float,
        v: Float,
    ) {
        val blockPosition = BlockPos(
            floor(originX + x.toDouble()).toInt(),
            floor(originY + y.toDouble()).toInt(),
            floor(originZ + z.toDouble()).toInt(),
        )
        val light =
            WorldRenderer.getLightmapCoordinates(world, blockPosition)

        // NOTE: Unlike in Java, where we can chain these method calls, in Kotlin we have to
        // specify the receiver explicitly for each one. This is because Kotlin resolves interface
        // methods a little differently: instead of always resolving dynamically, it will instead
        // always resolve default methods statically. Which is not what we want here, because that
        // gives us bad UVs.
        vertexBuffer.vertex(
            matrices.positionMatrix,
            x - thickness.x,
            y - thickness.y,
            z - thickness.z,
        )
        vertexBuffer.color(255, 255, 255, 255)
        vertexBuffer.texture(uLeft, v)
        vertexBuffer.light(light)
        vertexBuffer.next()

        vertexBuffer.vertex(
            matrices.positionMatrix,
            x + thickness.x,
            y + thickness.y,
            z + thickness.z,
        )
        vertexBuffer.color(255, 255, 255, 255)
        vertexBuffer.texture(uRight, v)
        vertexBuffer.light(light)
        vertexBuffer.next()
    }
}