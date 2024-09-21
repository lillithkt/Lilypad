package gay.lilyy.lilypad.core.osc

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.slimevr.oscquery.OSCQueryNode
import dev.slimevr.oscquery.OSCQueryServer
import dev.slimevr.oscquery.OscTransport
import dev.slimevr.oscquery.randomFreePort
import gay.lilyy.lilypad.core.Utils
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier

const val vrcOSCStartsWith = "VRChat-Client"
object OSCQuery {
    var server: OSCQueryServer

    var oscPort: MutableState<Int?> = mutableStateOf(null)
    var oscAddress: MutableState<String?> = mutableStateOf(null)

    var oscQPort: MutableState<Int?> = mutableStateOf(null)
    var oscQAddress: MutableState<String?> = mutableStateOf(null)

    fun update() {
        if (!CoreModules.Core.config!!.oscQuery) {
            server.close()
            return
        }
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Updating OSCQuery address to ${CoreModules.Core.config!!.listen}")
        server.updateOscService(CoreModules.Core.config!!.listen.toUShort())
    }

    init {
        System.setProperty("java.net.preferIPv4Stack", "true") // https://github.com/jmdns/jmdns/issues/244
        val httpPort = randomFreePort()
        server = OSCQueryServer("Lilypad", OscTransport.UDP, Utils.getLocalIp(), CoreModules.Core.config!!.listen.toUShort(), httpPort)
        server.rootNode.addNode(OSCQueryNode("/avatar"))


        // Actual OSC
        server.service.addServiceListener(
            "_osc._udp.local.",
            onServiceAdded = {
                if (!it.name.startsWith(vrcOSCStartsWith)) return@addServiceListener
                oscAddress.value = it.inetAddresses.first().hostAddress
                oscPort.value = it.port
                if (CoreModules.Core.config!!.logs.debug) Napier.d("Found VRChat client OSC at $oscAddress:$oscPort")
                OSCSender.updateAddress()
            },
            onServiceRemoved = { _, name ->
                if (!name.startsWith(vrcOSCStartsWith)) return@addServiceListener
                oscAddress.value = null
                oscPort.value = null
                if (CoreModules.Core.config!!.logs.debug) Napier.d("Lost VRChat client OSC")
                OSCSender.updateAddress()
            }
        )

        // OSCQuery JSON
        server.service.addServiceListener(
            "_oscjson._tcp.local.",
            onServiceAdded = {
                if (!it.name.startsWith(vrcOSCStartsWith)) return@addServiceListener
                oscQAddress.value = it.inetAddresses.first().hostAddress
                oscQPort.value = it.port
                if (CoreModules.Core.config!!.logs.debug) Napier.d("Found VRChat client OSCQJson at $oscQAddress:$oscQPort")
            },
            onServiceRemoved = { _, name ->
                if (!name.startsWith(vrcOSCStartsWith)) return@addServiceListener
                oscQAddress.value = null
                oscQPort.value = null
                if (CoreModules.Core.config!!.logs.debug) Napier.d("Lost VRChat client OSCQJson")
            }
        )

        server.init()
    }
}