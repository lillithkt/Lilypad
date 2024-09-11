package gay.lilyy.lilypad.core.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.OSCPortOut
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.core.Core
import java.net.InetAddress

object OSCSender {
    lateinit var sender: OSCPortOut

    fun updateAddress() {
        println("Updating OSC sender address to ${(Modules.modules["Core"] as Core).config!!.connect}")
        val (address, port) = (Modules.modules["Core"] as Core).config!!.connect.split(":")
        sender = OSCPortOut(InetAddress.getByName(address), port.toInt())
    }

    init {
        updateAddress()
    }

    fun send(message: OSCMessage) {
        sender.send(message)
    }
}
