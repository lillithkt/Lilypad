package gay.lilyy.lilypad.core.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCSerializerAndParserBuilder
import com.illposed.osc.argument.handler.Activator
import com.illposed.osc.transport.OSCPortOut
import gay.lilyy.lilypad.core.modules.CoreModules
import io.github.aakira.napier.Napier
import java.net.InetAddress
import java.net.InetSocketAddress


object OSCSender {
    lateinit var sender: OSCPortOut

    fun updateAddress() {
        val port = OSCQuery.oscPort.value ?: CoreModules.Core.config!!.connect.split(":")[1].toInt()
        val address = OSCQuery.oscAddress.value ?: CoreModules.Core.config!!.connect.split(":")[0]
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Updating OSC sender address to $address:$port")
        val serializer = OSCSerializerAndParserBuilder()
        serializer.setUsingDefaultHandlers(false)
        val defaultParserTypes = Activator.createSerializerTypes()
        defaultParserTypes.removeAt(16)
        var typeChar = 'a'
        for (argumentHandler in defaultParserTypes) {
            serializer.registerArgumentHandler(argumentHandler, typeChar)
            typeChar++
        }
        sender = OSCPortOut(serializer, InetSocketAddress(InetAddress.getByName(address), port))
    }

    init {
        updateAddress()
    }

    fun send(message: OSCMessage) {
        Thread {
        if (CoreModules.Core.config!!.logs.outgoingData) Napier.v("Sending OSC message: ${message.toFormattedString()}")
        sender.send(message)
        }.start()
    }
}
