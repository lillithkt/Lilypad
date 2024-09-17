package gay.lilyy.lilypad.core.modules.modules.template

import androidx.compose.runtime.*
import gay.lilyy.lilypad.core.modules.Module
import kotlinx.serialization.Serializable

@Serializable
data class TemplateConfig(
    var enabled: Boolean = false
)

@Suppress("unused")
class Template : Module<TemplateConfig>() {
    override val name = "Template"

    override val configClass = TemplateConfig::class

    override val hasSettingsUI = true
    @Composable
    override fun onSettingsUI() {
    }
}