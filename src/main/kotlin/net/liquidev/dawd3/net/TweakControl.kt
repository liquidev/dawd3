package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.ControlName
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

data class TweakControl(val position: BlockPos, val control: Identifier, val data: ByteArray) {
    companion object {
        val id = Identifier(Mod.id, "tweak_control")

        fun registerServerReceiver() {
            ServerPlayNetworking.registerGlobalReceiver(id) { server, player, _, buffer, _ ->
                // TODO: This should check for distance or something along those lines
                //  to prevent cheating. We aren't currently performing any such checks because
                //  it's kind of hard (as a rack may span tens of meters with many controls.)

                val packet = deserialize(buffer)
                server.execute {
                    val blockEntity =
                        player.world.getBlockEntity(packet.position) as? DeviceBlockEntity
                            ?: return@execute
                    val controlName =
                        ControlName.fromString(packet.control.toString()) ?: return@execute
                    val control = blockEntity.controlMap[controlName] ?: return@execute
                    control.valueFromBytes(packet.data)

                    val witnesses = PlayerLookup.tracking(blockEntity)
                    for (witness in witnesses) {
                        // The sending player is the person tweaking, we don't wanna overwrite their
                        // changes spuriously if their connection is laggy.
                        if (witness != player) {
                            ServerPlayNetworking.send(
                                witness,
                                id,
                                TweakControl(
                                    packet.position,
                                    packet.control,
                                    packet.data,
                                ).serialize()
                            )
                        }
                    }
                }
            }
        }

        fun registerClientReceiver() {
            ClientPlayNetworking.registerGlobalReceiver(id) { client, _, buffer, _ ->
                val packet = deserialize(buffer)
                client.execute {
                    val world = client.world
                    if (world != null) {
                        val blockEntity =
                            world.getBlockEntity(packet.position) as? DeviceBlockEntity
                                ?: return@execute
                        val controlName =
                            ControlName.fromString(packet.control.toString()) ?: return@execute
                        val control = blockEntity.controlMap[controlName]
                        control?.valueFromBytes(packet.data)
                    }
                }
            }
        }

        private fun deserialize(buffer: PacketByteBuf) =
            TweakControl(
                position = buffer.readBlockPos(),
                control = buffer.readIdentifier(),
                data = buffer.readByteArray(),
            )
    }

    fun serialize(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeBlockPos(position)
        buffer.writeIdentifier(control)
        buffer.writeByteArray(data)
        return buffer
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TweakControl

        if (position != other.position) return false
        if (control != other.control) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + control.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}