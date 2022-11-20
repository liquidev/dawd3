package net.liquidev.dawd3.audio.device

class DeviceInstance(val state: Device) {
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

    fun process(sampleCount: Int, channels: Int) {
        state.process(sampleCount, channels)
    }

    override fun toString(): String {
        return "DeviceInstance($state)"
    }
}