package net.liquidev.dawd3.net

object Packets {
    fun registerClientReceivers() {
        StartConnectingPorts.registerClientReceiver()
        ConnectPorts.registerClientReceiver()
        DisconnectPort.registerClientReceiver()
        TweakControl.registerClientReceiver()
    }

    fun registerServerReceivers() {
        TweakControl.registerServerReceiver()
        ControlTweaked.registerServerReceiver()
        EditRack.registerServerReceiver()
        ReorderRack.registerServerReceiver()
    }
}