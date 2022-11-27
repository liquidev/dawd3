package net.liquidev.dawd3.audio.device

class ProcessingState {
    val processedDevices = mutableSetOf<DeviceInstance>()

    fun reset() {
        processedDevices.clear()
    }
}