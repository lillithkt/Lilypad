package gay.lilyy.lilypad.core.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.OSCPortOut
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.core.Core
import io.github.aakira.napier.Napier
import java.net.InetAddress


object OSCSender {
    lateinit var sender: OSCPortOut

    fun updateAddress() {
        println("Updating OSC sender address to ${Modules.get<Core>("Core")!!.config!!.connect}")
        val (address, port) = Modules.get<Core>("Core")!!.config!!.connect.split(":")
        sender = OSCPortOut(InetAddress.getByName(address), port.toInt())
    }

    init {
        updateAddress()
    }

    fun send(message: OSCMessage) {
        if (Modules.get<Core>("Core")!!.config!!.logs.outgoingData) Napier.v("Sending OSC message: ${message.toFormattedString()}")
        sender.send(message)
    }
}
