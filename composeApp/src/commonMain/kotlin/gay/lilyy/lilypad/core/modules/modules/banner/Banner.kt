package gay.lilyy.lilypad.core.modules.modules.banner

import androidx.compose.material.*
import androidx.compose.runtime.*
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import kotlinx.serialization.Serializable

@Serializable
data class BannerSet(
    var name: String = "",
    var enabled: Boolean = false,
    var messages: MutableList<String> = mutableListOf()
)

@Serializable
data class BannerConfig(
    var enabled: Boolean = false,
    var updateInterval: Int = 5000,
    var random: Boolean = false,
    var sets: MutableList<BannerSet> = mutableListOf()
)

@Suppress("unused")
class Banner : ChatboxModule<BannerConfig>() {
    override val name = "Banner"

    override val configClass = BannerConfig::class

    override val hasSettingsUI = true
    @Composable
    override fun onSettingsUI() {
        var enabled by remember { mutableStateOf(config!!.enabled) }

        Text("Enabled")
        Checkbox(
            checked = enabled,
            onCheckedChange = { enabled = it
                config!!.enabled = it
                saveConfig()
            }
        )

        var updateInterval by remember { mutableStateOf(config!!.updateInterval) }

        Text("Update Interval")
        TextField(
            value = updateInterval.toString(),
            onValueChange = {
                updateInterval = it.toIntOrNull() ?: 5000
                config!!.updateInterval = updateInterval
                saveConfig()
            }
        )

        var random by remember { mutableStateOf(config!!.random) }

        Text("Random")
        Checkbox(
            checked = random,
            onCheckedChange = { random = it
                config!!.random = it
                saveConfig()
            }
        )

        // mutableList to mutableStateList
        val sets = config!!.sets.toMutableStateList()

        sets.forEach { set ->
            var setOpen by remember { mutableStateOf(false) }
            Button(onClick = { setOpen = !setOpen }) {
                Text(set.name)
            }
            if (setOpen) {
                var setEnabled by remember { mutableStateOf(set.enabled) }
                var setName by remember { mutableStateOf(set.name) }

                Text("Name")
                TextField(
                    value = setName,
                    onValueChange = {
                        setName = it
                        set.name = it
                        config!!.sets = sets
                        saveConfig()
                    }
                )

                Text("Enabled")
                Checkbox(
                    checked = setEnabled,
                    onCheckedChange = { setEnabled = it
                        set.enabled = it
                        config!!.sets = sets
                        saveConfig()
                    }
                )

                var messages by remember { mutableStateOf(set.messages.joinToString("\n")) }

                Text("Messages")
                TextField(
                    value = messages,
                    onValueChange = {
                        messages = it
                        set.messages = it.split("\n").toMutableList()
                        config!!.sets = sets
                        saveConfig()
                    }
                )
                Button(onClick = {
                    sets.remove(set)
                    config!!.sets = sets
                    saveConfig()
                }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)) {
                    Text("Delete Set")
                }
            }


        }

        Button(onClick = {
            val name = "Set ${config!!.sets.size + 1}"
            sets.add(BannerSet(name = name))
            config!!.sets = sets
            saveConfig()
        }) {
            Text("Add Set")
        }
    }

    private fun getRandom(): Int {
        return (0..Int.MAX_VALUE).random()
    }

    private var random: Int = getRandom()

    override val chatboxBuildOrder = -1
    override fun buildChatbox(): List<String> {
        if (!config!!.enabled) return emptyList()
        val possibleMessages = config!!.sets.filter { it.enabled }.flatMap { it.messages }
        return listOf(possibleMessages[random % possibleMessages.size])
    }

    override fun init() {
        super.init()

        Thread {
            while (true) {
                if (config!!.random) {
                    random = getRandom()
                } else {
                    random += 1

                    if (random == Int.MAX_VALUE) {
                        random = 0
                    }
                }
                Thread.sleep(config!!.updateInterval.toLong())
            }
        }.start()
    }
}