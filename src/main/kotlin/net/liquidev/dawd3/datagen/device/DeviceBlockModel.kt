package net.liquidev.dawd3.datagen.device

import com.google.gson.JsonObject
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.common.*
import net.liquidev.dawd3.datagen.json.element
import net.liquidev.dawd3.datagen.json.face
import net.liquidev.dawd3.datagen.json.faces
import net.liquidev.dawd3.datagen.json.uvRect
import net.liquidev.dawd3.datagen.jsonArray
import net.liquidev.dawd3.datagen.jsonObject
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

object DeviceBlockModel {
    private val portSize = Vec2f(3f, 3f)
    private const val portTexture = "${Mod.id}:device/port"

    private fun getSideTexture(id: Identifier) =
        "${Mod.id}:block/${id.path}_side"

    private fun getFrontTexture(id: Identifier) =
        "${Mod.id}:block/${id.path}_front"

    fun generate(
        id: Identifier,
        deviceBlock: Blocks.RegisteredDeviceBlock,
    ): JsonObject {
        return jsonObject {
            add("parent", "block/block")

            add("textures", jsonObject {
                add("side", getSideTexture(id))
                add("front", getFrontTexture(id))
                add("port", portTexture)
                add("particle", "#side")
            })

            add("elements", jsonArray {
                add(blockElement)

                val descriptor = deviceBlock.descriptor
                for (port in descriptor.portLayout) {
                    add(getPortElement(port))
                }
            })
        }
    }

    private val blockElement = element(
        from = Vec3f(0f, 0f, 0f),
        to = Vec3f(16f, 16f, 16f),
        faces = faces(
            north = face(texture = "#front", cullface = "north"),
            east = face(texture = "#side", cullface = "east"),
            south = face(texture = "#side", cullface = "south"),
            west = face(texture = "#side", cullface = "west"),
            up = face(texture = "#side", cullface = "up"),
            down = face(texture = "#side", cullface = "down"),
        )
    )

    private val portPlayArea = Vec2f(16f, 16f) - portSize
    private fun relativeToAbsolutePortPosition(position: Vec2f): Vec2f {
        val inv = Vec2f(1f - position.x, 1f - position.y)
        return inv * portPlayArea
    }

    private fun getPortElement(port: PhysicalPort): JsonObject {
        val bottomLeft2D = relativeToAbsolutePortPosition(port.position)
        val topRight2D = bottomLeft2D + portSize
        val bottomLeft = Vec3f(bottomLeft2D.x, bottomLeft2D.y, -0.01f)
        val topRight = Vec3f(topRight2D.x, topRight2D.y, 0.01f)

        val rotatedBottomLeft = port.side.transform.timesXZ(bottomLeft)
        val rotatedTopRight = port.side.transform.timesXZ(topRight)

        val min = rotatedBottomLeft.min(rotatedTopRight)
        val max = rotatedBottomLeft.max(rotatedTopRight)

        return element(
            from = min,
            to = max,
            faces = jsonObject {
                val faceName = port.side.face.getName()
                add(
                    faceName, face(
                        texture = "#port",
                        cullface = faceName,
                        uv = uvRect(0f, 0f, 4f, 4f)
                    )
                )
            }
        )
    }
}