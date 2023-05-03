package net.liquidev.dawd3.audio.math

inline fun oversample(
    phase: Float,
    deltaPhase: Float,
    sampleCount: Int,
    oscillator: (Float, Float) -> Float,
): Float {
    var accumulator = 0f
    for (sample in 0 until sampleCount) {
        val t = sample.toFloat() / sampleCount.toFloat()
        accumulator += oscillator(phase + deltaPhase * t, deltaPhase)
    }
    return accumulator / sampleCount.toFloat()
}

fun polyBLEP(t: Float, dt: Float) =
    if (t < dt) {
        val tt = t / dt
        tt + tt - tt * tt - 1f
    } else if (t > 1f - dt) {
        val tt = (t - 1f) / dt
        tt * tt + tt + tt + 1f
    } else {
        0f
    }
