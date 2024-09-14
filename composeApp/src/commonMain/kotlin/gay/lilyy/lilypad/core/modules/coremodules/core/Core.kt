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

    init {
        init()
    }

    override val hasSettingsUI = true

    @Composable
    override fun onSettingsUI() {
        var listenPort by remember { mutableStateOf(config!!.listen) }
        var connectAddress by remember { mutableStateOf(config!!.connect) }

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
            value = connectAddress,
            onValueChange = { connectAddress = it
                validateConnectAddress()
            },
            label = { Text("Connect address") }
        )

        Button(onClick = {
            config!!.listen = listenPort
            config!!.connect = connectAddress
            saveConfig()
            OSCSender.updateAddress()
            OSCReceiver.updateAddress()
            OSCQuery.updateAddress()
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
            TextField(
                value = outgoingChatbox.toString(),
                onValueChange = { outgoingChatbox = it.toBoolean()
                                config!!.logs.outgoingChatbox = it.toBoolean()
                    saveConfig()
                },
            )
            Text("Incoming OSC")
            Checkbox(
                checked = config!!.logs.incomingData,
                onCheckedChange = {
                    config!!.logs.incomingData = it
                    saveConfig()
                }
            )
            Text("Outgoing OSC")
            Checkbox(
                checked = config!!.logs.outgoingData,
                onCheckedChange = {
                    config!!.logs.outgoingData = it
                    saveConfig()
                }
            )
            Text("Errors")
            Checkbox(
                checked = config!!.logs.errors,
                onCheckedChange = {
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