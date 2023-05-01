package net.liquidev.dawd3.audio.unit

import kotlin.math.pow


@JvmInline
value class VOct(val value: Float) {
    fun toFrequency(a4: Frequency): Frequency = Frequency(a4.value * 2f.pow(value / 12f))
}

@JvmInline
value class Frequency(val value: Float)