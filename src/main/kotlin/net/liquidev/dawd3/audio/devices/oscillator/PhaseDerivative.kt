package net.liquidev.dawd3.audio.devices.oscillator

import kotlin.math.abs

/** Utility for computing the derivative of a phase without artifacting. */
class PhaseDerivative {
    private var previousPhase = 0f
    private var previousDeltaPhase = 0f

    fun stepNextDerivative(phase: Float): Float {
        val deltaPhase = phase - previousPhase
        val smoothDeltaPhase = if (abs(deltaPhase) > 0.999) previousDeltaPhase else deltaPhase
        previousPhase = phase
        previousDeltaPhase = deltaPhase
        return smoothDeltaPhase
    }
}