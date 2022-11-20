package net.liquidev.dawd3.audio.device

/** Device that can process audio. */
interface Device {
    fun process(sampleCount: Int, channels: Int)

    fun visitInputPorts(visit: (PortName, InputPort) -> Unit) {}
    fun visitOutputPorts(visit: (PortName, OutputPort) -> Unit) {}
}
