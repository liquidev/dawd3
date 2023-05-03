package net.liquidev.dawd3.audio.math

class Trigger {
    var isTriggered = false

    fun stepEdge(nextSample: Float): Edge {
        val wasTriggered = isTriggered
        isTriggered = nextSample > 0.5
        return if (!wasTriggered && isTriggered) {
            Edge.Rising
        } else if (wasTriggered && !isTriggered) {
            Edge.Falling
        } else {
            Edge.Constant
        }
    }

    enum class Edge {
        Constant,
        Rising,
        Falling,
    }
}