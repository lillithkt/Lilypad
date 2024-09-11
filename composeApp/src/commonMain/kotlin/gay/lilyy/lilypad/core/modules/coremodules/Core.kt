package gay.lilyy.lilypad.core.modules.coremodules

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import gay.lilyy.lilypad.config.ConfigStorage
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.core.osc.OSCReceiver
import gay.lilyy.lilypad.core.osc.OSCSender

class Core : Module() {
    override val name = "Core"

    override val hasSettingsUI = true

    @Composable
    override fun onSettingsUI() {
        var listenPort by remember { mutableStateOf(ConfigStorage.all.core.listen) }
        var connectAddress by remember { mutableStateOf(ConfigStorage.all.core.connect) }

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
            ConfigStorage.all.core.listen = listenPort
            ConfigStorage.all.core.connect = connectAddress
            ConfigStorage.save()
            OSCSender.updateAddress()
            OSCReceiver.updateAddress()
        }, enabled = connectAddressValid) {
            Text("Save")
        }
    }
}