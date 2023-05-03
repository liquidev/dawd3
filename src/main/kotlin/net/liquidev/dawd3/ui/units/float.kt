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

class SiValue(val unit: String) : FloatUnit {
    override fun display(value: Float): String {
        val divided: Float =
            if (value > 1_000_000_000_000) value / 1_000_000_000_000
            else if (value > 1_000_000_000) value / 1_000_000_000
            else if (value > 1_000_000) value / 1_000_000
            else if (value > 1_000) value / 1_000
            else if (value < 1) value * 1_000
            else value
        val prefix =
            if (value > 1_000_000_000_000) "T"
            else if (value > 1_000_000_000) "G"
            else if (value > 1_000_000) "M"
            else if (value > 1_000) "k"
            else if (value < 1) "m"
            else ""
        return String.format("%.01f %s%s", divided, prefix, unit)
    }

    companion object {
        val time = SiValue("s")
    }
}
