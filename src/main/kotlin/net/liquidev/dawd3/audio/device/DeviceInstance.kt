package net.liquidev.dawd3.audio.device

class DeviceInstance private constructor(val state: Device<ControlSet>, val controls: ControlSet) {
    val inputPortsByName = hashMapOf<PortName, InputPort>()
    val outputPortsByName = hashMapOf<PortName, OutputPort>()

    init {
        state.visitInputPorts { portName, inputPort ->
            ensureUniquePort(portName)
            inputPortsByName[portName] = inputPort
            inputPort.owningDevice = this
        }
        state.visitOutputPorts { portName, outputPort ->
            ensureUniquePort(portName)
            outputPortsByName[portName] = outputPort
            outputPort.owningDevice = this
        }
    }

    private fun ensureUniquePort(name: PortName) {
        if (name in inputPortsByName || name in outputPortsByName) {
            throw PortAlreadyExistsException("port $name is already registered in device $this")
        }
    }

    fun process(sampleCount: Int, channels: Int, processingState: ProcessingState) {
        if (this !in processingState.processedDevices) {
            processingState.processedDevices.add(this)
            for ((_, port) in inputPortsByName) {
                port.connectedOutput?.owningDevice?.process(sampleCount, channels, processingState)
            }
            state.process(sampleCount, controls)
        }
    }

    override fun toString(): String {
        return "DeviceInstance($state)"
    }

    companion object {
        fun <T : ControlSet> create(state: Device<T>, controls: T) =
            // NOTE: We're erasing the type from concrete T to Controls, the cast is fine.
            @Suppress("UNCHECKED_CAST")
            DeviceInstance(state as Device<ControlSet>, controls)
    }
}