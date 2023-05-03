package net.liquidev.dawd3.ui.units

import net.liquidev.dawd3.audio.unit.Amplitude
import kotlin.math.roundToInt

interface FloatUnit {
    fun display(value: Float): String
}

object RawValue : FloatUnit {
    override fun display(value: Float) = String.format("%.02f", value)
}

object AmplitudeValue : FloatUnit {
    override fun display(value: Float): String {
        val decibels = Amplitude(value).toDecibels().value
        return if (decibels == Float.NEGATIVE_INFINITY) {
            "-∞ dB"
        } else {
            String.format("%.01f dB", decibels)
        }
    }
}

object PercentageValue : FloatUnit {
    override fun display(value: Float) =
        String.format("%d%%", (value * 100f).roundToInt())
}
