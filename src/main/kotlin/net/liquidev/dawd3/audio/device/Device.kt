package net.liquidev.dawd3.audio.device

/** Device that can process audio. */
interface Device {
    fun process(sampleCount: Int, channels: Int)

    fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {}
    fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {}
}
