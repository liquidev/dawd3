package net.liquidev.dawd3.net

object Packets {
    fun registerClientReceivers() {
        StartConnectingPorts.registerClientReceiver()
        ConnectPorts.registerClientReceiver()
    }
}