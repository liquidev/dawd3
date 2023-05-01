package net.liquidev.dawd3.audio.device

/** Device that can process audio. */
interface Device<C : ControlSet> {
    fun process(sampleCount: Int, controls: C)

    fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {}
    fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {}
}
