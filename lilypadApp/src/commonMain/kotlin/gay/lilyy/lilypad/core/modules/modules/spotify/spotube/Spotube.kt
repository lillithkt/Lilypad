package gay.lilyy.lilypad.core.modules.modules.spotify.spotube

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import gay.lilyy.lilypad.core.modules.CoreModules
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

data class SpotubeState(
    var name: String,
    var playing: Boolean,
    var currentTrack: SpotubeTrack?,
    var currentTrackPosition: Int?
)

object Spotube {
    var jmdns: JmDNS? = null
    val clients: SnapshotStateMap<String, SpotubeState> = mutableStateMapOf()

    val json = Json {
        ignoreUnknownKeys = true
    }

    var selectedClient: MutableState<String?> = mutableStateOf(null)

    val currentClient: SpotubeState?
        get() =
            if (selectedClient.value == null) clients.values.firstOrNull()
            else clients[selectedClient.value]


    private val client: HttpClient = HttpClient(CIO) {
        install(WebSockets)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun addRemote(serviceInfo: ServiceInfo) {
        val address = try {
            serviceInfo.inetAddresses.first().hostAddress
        } catch (e: Exception) {
            if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to get address for Spotube service ${serviceInfo.name}")
            return
        }
        val port = serviceInfo.port
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Found Spotube service ${serviceInfo.name} at $address:$port")
        clients["$address:$port"] = SpotubeState(serviceInfo.name, false, null, null)
        scope.launch {
            try {
                client.webSocket(
                    "ws://$address:$port/ws"
                ) {
                    if (CoreModules.Core.config!!.logs.debug) Napier.d("Connected to Spotube service ${serviceInfo.name}")
                    while (true) {
                        val frame = incoming.receive() as? Frame.Text ?: continue
                        val text = frame.readText()
                        if (CoreModules.Core.config!!.logs.incomingData) Napier.v("Received Spotube message: $text")
                        val type = json.decodeFromString(BaseWsEvent.serializer(), text).type

                        // iterate over WsEvents enum and call the handler if the type matches
                        for (event in WsEvents.entries) {
                            if (event.type == type) {
                                event.handler(text, "$address:$port")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                removeRemote(serviceInfo)
            }

        }
    }

    private fun removeRemote(serviceInfo: ServiceInfo) {
        val address = try {
            serviceInfo.inetAddresses.first().hostAddress
        } catch (e: Exception) {
            if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to get address for Spotube service ${serviceInfo.name}")
            return
        }
        val port = serviceInfo.port
        if ("$address:$port" !in clients) return
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Lost Spotube service ${serviceInfo.name} at $address:$port")
        clients.remove("$address:$port")
        if (selectedClient.value == "$address:$port") {
            selectedClient.value = null
        }
    }

    fun init() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            jmdns = JmDNS.create(InetAddress.getLocalHost())
        }
        val listener = object : javax.jmdns.ServiceListener {
            override fun serviceAdded(event: javax.jmdns.ServiceEvent) {
                jmdns!!.getServiceInfo(event.type, event.name)?.let {
                    addRemote(it)
                }
            }

            override fun serviceRemoved(event: javax.jmdns.ServiceEvent) {
                removeRemote(event.info)
            }

            override fun serviceResolved(event: javax.jmdns.ServiceEvent) {}
        }
        jmdns!!.addServiceListener("_spotube._tcp.local.", listener)
        jmdns!!.list("_spotube._tcp.local.").forEach {
            addRemote(it)
        }
    }
}