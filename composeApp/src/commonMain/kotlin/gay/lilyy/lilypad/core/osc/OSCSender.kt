package gay.lilyy.lilypad.core.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.OSCPortOut
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier
import java.net.InetAddress


object OSCSender {
    lateinit var sender: OSCPortOut

    fun updateAddress() {
        val port = OSCQuery.oscPort.value ?: CoreModules.Core.config!!.connect.split(":")[1].toInt()
        val address = OSCQuery.oscAddress.value ?: CoreModules.Core.config!!.connect.split(":")[0]
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Updating OSC sender address to $address:$port")
        sender = OSCPortOut(InetAddress.getByName(address), port)
    }

    init {
        updateAddress()
    }

    fun send(message: OSCMessage) {
        if (CoreModules.Core.config!!.logs.outgoingData) Napier.v("Sending OSC message: ${message.toFormattedString()}")
        sender.send(message)
    }
}
