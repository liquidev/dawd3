package net.liquidev.dawd3.audio.device

import net.liquidev.dawd3.audio.AudioBuffer

class InputPort {
    private companion object {
        val emptyBuffer = AudioBuffer()
    }

    var connectedOutput: OutputPort? = null
    lateinit var owningDevice: DeviceInstance

    /**
     * Convenience function that returns an empty buffer if there is no connected port, or the
     * connected port doesn't have a buffer with the given index.
     */
    fun getConnectedOutputBuffer(index: Int, sampleCount: Int): FloatArray {
        val connectedOutput = connectedOutput
        return if (connectedOutput != null && index < connectedOutput.buffers.size) {
            connectedOutput.buffers[index].array ?: emptyBuffer.getOrReallocate(sampleCount)
        } else {
            emptyBuffer.getOrReallocate(sampleCount)
        }
    }
}

class OutputPort(bufferCount: Int) {
    init {
        require(bufferCount >= 1) { "output port must have at least one buffer" }
    }

    val buffers = Array(bufferCount) { AudioBuffer() }
    val connectedInputs = hashSetOf<InputPort>()
    lateinit var owningDevice: DeviceInstance
}

/** Marker class that port name markers should inherit from. */
abstract class PortName
