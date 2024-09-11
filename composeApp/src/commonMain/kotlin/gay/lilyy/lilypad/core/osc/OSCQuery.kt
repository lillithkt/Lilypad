package gay.lilyy.lilypad.core.osc

import dev.slimevr.oscquery.OSCQueryNode
import dev.slimevr.oscquery.OSCQueryServer
import dev.slimevr.oscquery.OscTransport
import dev.slimevr.oscquery.randomFreePort
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.core.Core
import io.github.aakira.napier.Napier

object OSCQuery {
    var server: OSCQueryServer

    fun updateAddress() {
        Napier.d("Updating OSCQuery address to ${Modules.get<Core>("Core")!!.config!!.listen}")
        server.updateOscService(Modules.get<Core>("Core")!!.config!!.listen.toUShort())

    }

    init {
        // TODO: Fix this
        val port = randomFreePort()
        server = OSCQueryServer("Lilypad", OscTransport.TCP, "127.0.0.1", Modules.get<Core>("Core")!!.config!!.listen.toUShort(), port)
        server.rootNode.addNode(OSCQueryNode("/tracking/vrsystem"))
        server.service.addServiceListener(
            "_osc._udp.local.",
            onServiceResolved = { println("Service resolved: $it") },
            onServiceAdded = { println("Service added: $it") },
        )
        server.init()

        server.createOscService()
    }
}