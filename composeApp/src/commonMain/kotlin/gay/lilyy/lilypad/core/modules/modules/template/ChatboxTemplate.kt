package gay.lilyy.lilypad.core.modules.modules.template

import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class ChatboxTemplateConfig(
    var enabled: Boolean = false
)

@Suppress("unused")
class ChatboxTemplate : ChatboxModule<ChatboxTemplateConfig>() {
    override val name = "ChatboxTemplate"
    override val disabled = true // Remove this line!

    override val configClass = ChatboxTemplateConfig::class

    override val hasSettingsUI = true
    @Composable
    override fun onSettingsUI() {
        var enabled by remember { mutableStateOf(config!!.enabled) }
        Text("Enabled")
        Checkbox(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                config!!.enabled = it
            }
        )
    }

    override val chatboxBuildOrder = 1
    override fun buildChatbox(): List<String> {
        if (!config!!.enabled) return emptyList()
        return listOf("Hello, World!")
    }
}