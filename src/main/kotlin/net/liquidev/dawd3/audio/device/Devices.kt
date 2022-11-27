package net.liquidev.dawd3.audio.device

import net.liquidev.dawd3.Mod

/** Device utility functions. */
object Devices {
    private val logger = Mod.logger<Devices>()

    data class InputAndOutputDevicePortPairs<T>(
        val outputDevice: T,
        val outputPort: OutputPortName,
        val inputDevice: T,
        val inputPort: InputPortName,
    )

    fun <T> sortPortsByInputAndOutput(
        fromData: T,
        fromPort: PortName,
        toData: T,
        toPort: PortName,
    ): InputAndOutputDevicePortPairs<T>? =
        when {
            fromPort is OutputPortName && toPort is InputPortName -> InputAndOutputDevicePortPairs(
                outputDevice = fromData,
                outputPort = fromPort,
                inputDevice = toData,
                inputPort = toPort,
            )
            fromPort is InputPortName && toPort is OutputPortName -> InputAndOutputDevicePortPairs(
                outputDevice = toData,
                outputPort = toPort,
                inputDevice = fromData,
                inputPort = fromPort,
            )
            else -> null
        }

    fun makeConnection(
        from: DeviceInstance,
        outputPortName: PortName,
        to: DeviceInstance,
        inputPortName: PortName,
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
        logger.debug("made connection between ports $outputPortName ($outputPort) and $inputPortName ($inputPort)")
    }

    private fun severAllConnectionsInInputPort(inputPort: InputPort): Int {
        var total = 0
        val connectedOutput = inputPort.connectedOutput
        if (connectedOutput != null) {
            connectedOutput.connectedInputs.remove(inputPort)
            inputPort.connectedOutput = null
            total += 1
        }
        return total
    }

    private fun severAllConnectionsInOutputPort(outputPort: OutputPort): Int {
        var total = 0
        for (port in outputPort.connectedInputs) {
            port.connectedOutput = null
        }
        total += outputPort.connectedInputs.size
        outputPort.connectedInputs.clear()
        return total
    }

    /** Disconnects everything from the given port. Returns the number of ports disconnected. */
    fun severAllConnectionsInPort(device: DeviceInstance, portName: PortName): Int {
        var total = 0

        val inputPort = device.inputPortsByName[portName]
        if (inputPort != null) {
            total += severAllConnectionsInInputPort(inputPort)
        }

        val outputPort = device.outputPortsByName[portName]
        if (outputPort != null) {
            total += severAllConnectionsInOutputPort(outputPort)
        }

        return total
    }

    fun severAllConnectionsInDevice(device: DeviceInstance): Int {
        var total = 0
        for ((_, inputPort) in device.inputPortsByName) {
            total += severAllConnectionsInInputPort(inputPort)
        }
        for ((_, outputPort) in device.outputPortsByName) {
            total += severAllConnectionsInOutputPort(outputPort)
        }
        return total
    }

    private fun noSuchPort(device: DeviceInstance, name: PortName): NoSuchPortException =
        NoSuchPortException("device $device does not have port with name $name")
}