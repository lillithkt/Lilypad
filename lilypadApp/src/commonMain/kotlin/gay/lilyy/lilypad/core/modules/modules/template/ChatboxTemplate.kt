package gay.lilyy.lilypad.core.modules.modules.template

import androidx.compose.runtime.*
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import gay.lilyy.lilypad.ui.components.LabeledCheckbox
import kotlinx.serialization.Serializable

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
        LabeledCheckbox(
            label = "Enabled",
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