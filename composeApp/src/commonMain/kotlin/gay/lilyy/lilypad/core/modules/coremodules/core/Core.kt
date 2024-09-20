package gay.lilyy.lilypad.core.modules.coremodules.core

import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.core.osc.OSCQuery
import gay.lilyy.lilypad.core.osc.OSCReceiver
import gay.lilyy.lilypad.core.osc.OSCSender

class Core : Module<CoreConfig>() {
    override val name = "Core"

    override val configClass = CoreConfig::class

    override val hasSettingsUI = true

    @Composable
    override fun onSettingsUI() {
        var listenPort by remember { mutableStateOf(config!!.listen) }
        var connectAddress by remember { mutableStateOf(config!!.connect) }

        val oscQAddress by remember { OSCQuery.oscAddress }
        val oscQPort by remember { OSCQuery.oscPort }

        TextField(
            value = listenPort.toString(),
            onValueChange = { listenPort = it.toIntOrNull() ?: 0 },
            label = { Text("Listen port") }
        )
        var connectAddressValid by remember { mutableStateOf(true) }
        fun validateConnectAddress() {
            val (address, port) = connectAddress.split(":")
            connectAddressValid = address.isNotEmpty() && port.toIntOrNull() != null
        }

        if (!connectAddressValid) {
            Text("Invalid connect address")
        }
        TextField(
            value = if (oscQAddress != null && oscQPort != null) "${oscQAddress}:${oscQPort}" else connectAddress,
            onValueChange = { connectAddress = it
                validateConnectAddress()
            },
            enabled = oscQAddress == null && oscQPort == null,
            label = { Text("Connect address") }
        )

        Button(onClick = {
            config!!.listen = listenPort
            config!!.connect = connectAddress
            saveConfig()
            OSCSender.updateAddress()
            OSCReceiver.updateAddress()
            OSCQuery.update()
        }, enabled = connectAddressValid) {
            Text("Update OSC")
        }

        var logsOpen by remember { mutableStateOf(false) }
        Button(onClick = { logsOpen = !logsOpen }) {
            Text("Logs")
        }
        if (logsOpen) {
            var outgoingChatbox by remember { mutableStateOf(config!!.logs.outgoingChatbox) }
            Text("Outgoing chatbox")
            Checkbox(
                checked = outgoingChatbox,
                onCheckedChange = {
                    outgoingChatbox = it
                    config!!.logs.outgoingChatbox = it
                    saveConfig()
                }
            )
            var incomingOSC by remember { mutableStateOf(config!!.logs.incomingData) }
            Text("Incoming OSC")
            Checkbox(
                checked = incomingOSC,
                onCheckedChange = {
                    incomingOSC = it
                    config!!.logs.incomingData = it
                    saveConfig()
                }
            )
            var outgoingOSC by remember { mutableStateOf(config!!.logs.outgoingData) }
            Text("Outgoing OSC")
            Checkbox(
                checked = outgoingOSC,
                onCheckedChange = {
                    outgoingOSC = it
                    config!!.logs.outgoingData = it
                    saveConfig()
                }
            )
            var errors by remember { mutableStateOf(config!!.logs.errors) }
            Text("Errors")
            Checkbox(
                checked = errors,
                onCheckedChange = {
                    errors = it
                    config!!.logs.errors = it
                    saveConfig()
                }
            )
            var warnings by remember { mutableStateOf(config!!.logs.warnings) }
            Text("Warnings")
            Checkbox(
                checked = warnings,
                onCheckedChange = {
                    warnings = it
                    config!!.logs.warnings = it
                    saveConfig()
                }
            )
            var debug by remember { mutableStateOf(config!!.logs.debug) }
            Text("Debug")
            Checkbox(
                checked = debug,
                onCheckedChange = {
                    debug = it
                    config!!.logs.debug = it
                    saveConfig()
                }
            )
        }
    }
}