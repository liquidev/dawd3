package net.liquidev.dawd3.audio.device

import net.liquidev.dawd3.Mod

/**
 * Sim is the simulator for device graphs.
 */
class Sim {
    private companion object {
        val logger = Mod.logger<Sim>()
    }

    private val stack = arrayListOf<DeviceInstance>()
    private val evalList = arrayListOf<DeviceInstance>()
    private val visited = arrayListOf<DeviceInstance>()

    /**
     * Builds a simulation sequence: establishes the order in which devices in a graph have to be
     * processed so that we arrive at the input signals fed into the terminal.
     */
    fun rebuild(terminal: DeviceInstance) {
        stack.add(terminal)

        evalList.clear()
        while (stack.isNotEmpty()) {
            val device = stack.removeAt(stack.size - 1)
            evalList.add(device)
            for ((_, inputPort) in device.inputPortsByName) {
                val connectedOutputPort = inputPort.connectedOutput
                if (connectedOutputPort != null) {
                    stack.add(connectedOutputPort.owningDevice)
                }
            }
        }

        logger.info("rebuilt device graph; new evaluation order (reversed): $evalList")
    }

    /** Process all devices in the built sequence. */
    fun simulate(sampleCount: Int, channels: Int) {
        for (device in evalList.asReversed()) {
            device.process(sampleCount, channels)
        }
    }
}