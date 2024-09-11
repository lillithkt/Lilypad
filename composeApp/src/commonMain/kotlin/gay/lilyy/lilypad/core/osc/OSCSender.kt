package gay.lilyy.lilypad.core.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.OSCPortOut
import gay.lilyy.lilypad.config.ConfigStorage
import java.net.InetAddress

object OSCSender {
    lateinit var sender: OSCPortOut

    fun updateAddress() {
        println("Updating OSC sender address to ${ConfigStorage.all.core.connect}")
        val (address, port) = ConfigStorage.all.core.connect.split(":")
        sender = OSCPortOut(InetAddress.getByName(address), port.toInt())
    }

    init {
        updateAddress()
    }

    fun send(message: OSCMessage) {
        sender.send(message)
    }
}
