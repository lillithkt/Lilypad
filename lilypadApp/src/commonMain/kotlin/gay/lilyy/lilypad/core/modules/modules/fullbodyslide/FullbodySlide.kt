package gay.lilyy.lilypad.core.modules.modules.template

import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.illposed.osc.OSCMessage
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.core.osc.OSCSender
import kotlinx.serialization.Serializable

@Serializable
data class FullbodySlideConfig(
    var enabled: Boolean = false
)

@Suppress("unused")
class FullbodySlide : Module<TemplateConfig>() {
    override val name = "Fullbody Slide"

    override val configClass = TemplateConfig::class

    override val hasSettingsUI = true
    @Composable
    override fun onSettingsUI() {
        Text("This module gives you fake trackers that allow you to do the fullbody slide", style = TextStyle(textAlign = TextAlign.Center))
        Text("(Please dont use this it looks so stupid, i added this as a joke)", style = MaterialTheme.typography.caption.merge(TextStyle(textAlign = TextAlign.Center)))
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

    override fun init() {
        super.init()
        Thread {
            while (true) {
                if (config!!.enabled) {
                    OSCSender.send(OSCMessage("/tracking/trackers/1/position", listOf(0f, 0f, 0f)))
                    OSCSender.send(OSCMessage("/tracking/trackers/1/rotation", listOf(0f, 0f, 0f)))
                }
                Thread.sleep(1000)
            }
        }.start()

    }
}