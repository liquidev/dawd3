package net.liquidev.dawd3.datagen.device

import com.google.gson.JsonObject
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.PortDirection
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.common.*
import net.liquidev.dawd3.datagen.json.element
import net.liquidev.dawd3.datagen.json.face
import net.liquidev.dawd3.datagen.json.faces
import net.liquidev.dawd3.datagen.json.uvRect
import net.liquidev.dawd3.datagen.jsonArray
import net.liquidev.dawd3.datagen.jsonObject
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

object DeviceBlockModel {
    val portSize = Vec2f(4f, 4f)
    private const val portTexture = "${Mod.id}:device/port"

    fun generate(deviceBlock: Blocks.RegisteredDeviceBlock): JsonObject {
        val faceTextures = deviceBlock.descriptor.faceTextures
        return jsonObject {
            add("parent", "block/block")

            add("textures", jsonObject {
                add("front", faceTextures.front.toString())
                add("back", faceTextures.back.toString())
                add("right", faceTextures.right.toString())
                add("left", faceTextures.left.toString())
                add("top", faceTextures.top.toString())
                add("bottom", faceTextures.bottom.toString())
                add("particle", faceTextures.particle.toString())
                add("port", portTexture)
            })

            add("elements", jsonArray {
                add(getBlockElement(deviceBlock.descriptor.cuboid))

                val descriptor = deviceBlock.descriptor
                for ((portName, port) in descriptor.portLayout) {
                    add(getPortElement(portName, port))
                }
            })
        }
    }

    private fun getBlockElement(cuboid: Box) = element(
        from = Vec3f(cuboid.minX.toFloat(), cuboid.minY.toFloat(), cuboid.minZ.toFloat()) * 16f,
        to = Vec3f(cuboid.maxX.toFloat(), cuboid.maxY.toFloat(), cuboid.maxZ.toFloat()) * 16f,
        faces = faces(
            north = face(texture = "#front", cullface = "north"),
            east = face(texture = "#right", cullface = "east"),
            south = face(texture = "#back", cullface = "south"),
            west = face(texture = "#left", cullface = "west"),
            up = face(texture = "#top", cullface = "up"),
            down = face(texture = "#bottom", cullface = "down"),
        )
    )

    private val portPlayArea = Vec2f(16f, 16f) - portSize
    fun relativeToAbsolutePortPosition(position: Vec2f): Vec2f {
        return position * portPlayArea
    }

    private fun getPortElement(portName: PortName, port: PhysicalPort): JsonObject {
        val bottomLeft2D =
            relativeToAbsolutePortPosition(Vec2f(1f - port.position.x, 1f - port.position.y))
        val topRight2D = bottomLeft2D + portSize
        val bottomLeft = Vec3f(bottomLeft2D.x, bottomLeft2D.y, -0.01f)
        val topRight = Vec3f(topRight2D.x, topRight2D.y, 0.01f)

        val rotatedBottomLeft = port.side.modelTransform.timesXZ(bottomLeft)
        val rotatedTopRight = port.side.modelTransform.timesXZ(topRight)

        val min = rotatedBottomLeft.min(rotatedTopRight)
        val max = rotatedBottomLeft.max(rotatedTopRight)

        val uvX = when (portName.direction) {
            PortDirection.Input -> 0f
            PortDirection.Output -> 4f
        }

        return element(
            from = min,
            to = max,
            faces = jsonObject {
                val faceName = port.side.face.direction.getName()
                add(
                    faceName, face(
                        texture = "#port",
                        cullface = faceName,
                        uv = uvRect(uvX, 0f, uvX + 4f, 4f)
                    )
                )
            }
        )
    }
}