package gay.lilyy.lilypad.core.osc

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.slimevr.oscquery.OSCQueryNode
import dev.slimevr.oscquery.OSCQueryServer
import dev.slimevr.oscquery.OscTransport
import dev.slimevr.oscquery.randomFreePort
import gay.lilyy.lilypad.core.Utils
import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier

object OSCQuery {
    var server: OSCQueryServer

    var port: MutableState<Int?> = mutableStateOf(null)
    var address: MutableState<String?> = mutableStateOf(null)

    fun update() {
        if (!Modules.Core.config!!.oscQuery) {
            server.close()
            return
        }
        if (Modules.Core.config!!.logs.debug) Napier.d("Updating OSCQuery address to ${Modules.Core.config!!.listen}")
        server.updateOscService(Modules.Core.config!!.listen.toUShort())
    }

    init {
        // TODO: Fix this
        val httpPort = randomFreePort()
        server = OSCQueryServer("Lilypad", OscTransport.UDP, Utils.getLocalIp(), Modules.Core.config!!.listen.toUShort(), httpPort)
        server.rootNode.addNode(OSCQueryNode("/avatar"))
        server.service.addServiceListener(
            "_osc._udp.local.",
            onServiceAdded = {
                if (!it.name.startsWith("VRChat-Client")) return@addServiceListener
                address.value = it.inetAddresses.first().hostAddress
                port.value = it.port
                if (Modules.Core.config!!.logs.debug) Napier.d("Found VRChat client at $address:$port")
                OSCSender.updateAddress()
            },
            onServiceRemoved = { _, name ->
                if (!name.startsWith("VRChat-Client")) return@addServiceListener
                address.value = null
                port.value = null
                if (Modules.Core.config!!.logs.debug) Napier.d("Lost VRChat client")
                OSCSender.updateAddress()
            }
        )
        server.init()

        server.createOscService()
    }
}