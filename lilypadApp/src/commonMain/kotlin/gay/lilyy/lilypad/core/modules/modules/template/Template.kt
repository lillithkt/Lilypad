package gay.lilyy.lilypad.core.modules.modules.template

import androidx.compose.runtime.*
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.ui.components.LabeledCheckbox
import kotlinx.serialization.Serializable

@Serializable
data class TemplateConfig(
    var enabled: Boolean = false
)

@Suppress("unused")
class Template : Module<TemplateConfig>() {
    override val name = "Template"
    override val disabled = true // Remove this line!

    override val configClass = TemplateConfig::class

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
}