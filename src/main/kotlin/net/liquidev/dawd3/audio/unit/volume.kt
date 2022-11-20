package net.liquidev.dawd3.audio.unit

import kotlin.math.log10
import kotlin.math.pow

@JvmInline
value class Amplitude(val value: Float) {
    fun toDecibels(): Decibels = Decibels(20.0f * log10(value))
}

@JvmInline
value class Decibels(val value: Float) {
    fun toAmplitude(): Amplitude = Amplitude(10.0f.pow(value / 20.0f))
}
