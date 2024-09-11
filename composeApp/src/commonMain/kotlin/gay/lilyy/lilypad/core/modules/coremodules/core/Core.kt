package gay.lilyy.lilypad.core.modules.coremodules.core

import androidx.compose.material.Button
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
            Text("Save")
        }
    }
}