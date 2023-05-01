package net.liquidev.dawd3.audio.device

import net.liquidev.dawd3.audio.AudioBuffer
import net.minecraft.util.Identifier

sealed class Port {
    lateinit var owningDevice: DeviceInstance
}

class InputPort : Port() {
    private companion object {
        val emptyBuffer = AudioBuffer()
    }

    var connectedOutput: OutputPort? = null

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

class OutputPort(bufferCount: Int) : Port() {
    init {
        require(bufferCount >= 1) { "output port must have at least one buffer" }
    }

    val buffers = Array(bufferCount) { AudioBuffer() }
    val connectedInputs = hashSetOf<InputPort>()
}

enum class PortDirection {
    Input,
    Output,
}

/** Marker interface that port name markers should inherit from. */
sealed interface PortName {
    val id: Identifier
    val direction: PortDirection

    companion object {
        private val registry = hashMapOf<Identifier, PortName>()

        internal fun register(name: PortName) {
            assert(name.id !in registry) { "there must not be two ports with the same name" }
            registry[name.id] = name
        }

        fun fromString(name: String): PortName? =
            registry[Identifier(name)]
    }
}

class InputPortName(override val id: Identifier) : PortName {
    override val direction = PortDirection.Input

    init {
        PortName.register(this)
    }

    constructor(
        parent: Identifier,
        name: String,
    ) : this(idInDevice(parent, name))

    override fun toString(): String = id.toString()
}

class OutputPortName private constructor(
    override val id: Identifier,
    private val instanceOf: OutputPortName?,
) : PortName {
    override val direction = PortDirection.Output

    init {
        PortName.register(this)
    }

    constructor(
        parent: Identifier,
        name: String,
    ) : this(idInDevice(parent, name), instanceOf = null)

    override fun toString(): String = id.toString()

    fun makeInstanced(instanceName: String) =
        OutputPortName(idInDevice(id, instanceName), instanceOf = this)

    fun resolveInstance() = instanceOf ?: this
}
