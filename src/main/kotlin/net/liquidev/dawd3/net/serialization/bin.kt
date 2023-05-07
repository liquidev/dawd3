package net.liquidev.dawd3.net.serialization

import net.minecraft.network.PacketByteBuf
import java.util.*

val zeroUuid = UUID(0L, 0L)

fun PacketByteBuf.readOptionalUuid(): UUID? {
    val uuid = readUuid()
    return if (uuid.leastSignificantBits == 0L && uuid.mostSignificantBits == 0L) {
        null
    } else {
        uuid
    }
}

fun PacketByteBuf.writeOptionalUuid(uuid: UUID?): PacketByteBuf {
    if (uuid != null) {
        writeUuid(uuid)
    } else {
        writeUuid(zeroUuid)
    }
    return this
}
