package net.liquidev.dawd3.audio.device

import net.minecraft.util.Identifier

internal fun idInDevice(deviceId: Identifier, name: String): Identifier =
    Identifier(deviceId.namespace, "${deviceId.path}/$name")
