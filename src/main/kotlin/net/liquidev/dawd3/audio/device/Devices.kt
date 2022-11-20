package net.liquidev.dawd3.audio.device

/** Device utility functions. */
object Devices {
    fun makeConnection(
        from: DeviceInstance,
        outputPortName: PortName,
        to: DeviceInstance,
        inputPortName: PortName
    ) {
        val outputPort = from.outputPortsByName[outputPortName]
        val inputPort = to.inputPortsByName[inputPortName]

        if (outputPort == null) {
            throw noSuchPort(from, outputPortName)
        }
        if (inputPort == null) {
            throw noSuchPort(to, inputPortName)
        }

        inputPort.connectedOutput = outputPort
        outputPort.connectedInputs.add(inputPort)
    }

    /** Disconnects everything from the given port. Returns the number of ports disconnected. */
    fun severAllConnections(device: DeviceInstance, portName: PortName): Int {
        var total = 0

        val inputPort = device.inputPortsByName[portName]
        if (inputPort != null) {
            val connectedOutput = inputPort.connectedOutput
            if (connectedOutput != null) {
                connectedOutput.connectedInputs.remove(inputPort)
                inputPort.connectedOutput = null
                total += 1
            }
        }

        val outputPort = device.outputPortsByName[portName]
        if (outputPort != null) {
            for (port in outputPort.connectedInputs) {
                port.connectedOutput = null
            }
            total += outputPort.connectedInputs.size
            outputPort.connectedInputs.clear()
        }

        return total
    }

    private fun noSuchPort(device: DeviceInstance, name: PortName): NoSuchPortException =
        NoSuchPortException("device $device does not have port with name $name")
}