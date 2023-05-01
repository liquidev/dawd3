package net.liquidev.dawd3.net

object Packets {
    fun registerClientReceivers() {
        StartConnectingPorts.registerClientReceiver()
        ConnectPorts.registerClientReceiver()
        TweakControl.registerClientReceiver()
    }

    fun registerServerReceivers() {
        TweakControl.registerServerReceiver()
        ControlTweaked.registerServerReceiver()
    }
}