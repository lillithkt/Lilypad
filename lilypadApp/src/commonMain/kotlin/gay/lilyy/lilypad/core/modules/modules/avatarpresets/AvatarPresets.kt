package gay.lilyy.lilypad.core.modules.modules.avatarpresets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.illposed.osc.OSCMessage
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.osc.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Preset(
    var name: String,
    var parameters: MutableMap<String, Parameter> = mutableMapOf()
)

@Serializable
data class Avatar(
    var name: String,
    val presets: MutableList<Preset> = mutableListOf()
)

@Serializable
data class AvatarPresetsConfig(
    val avatars: MutableMap<String, Avatar> = mutableMapOf()
)

@Suppress("unused")
class AvatarPresets : Module<AvatarPresetsConfig>() {
    override val name = "Avatar Presets"

    override val configClass = AvatarPresetsConfig::class

    override val hasSettingsUI = true

    private suspend fun savePreset(index: Int) {
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Saving preset $index")
        val rootNode = OSCQJson.getNode("/")
        if (rootNode === null) {
            if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to get root node")
            return
        }
        val parameters = mutableMapOf<String, Parameter>()

        fun recurseNode(node: ParameterNode) {
            if (node.contents != null) {
                for (content in node.contents) {
                    recurseNode(content.value)
                }
            } else {
                if (node.access !== Access.READ_WRITE) return
                if (node.value === null || node.value.isEmpty()) return
                parameters[node.fullPath] = node.value.first()
            }
        }
        recurseNode(rootNode)

        config!!.avatars[CoreModules.GameStorage.curAvatarId.value!!]!!.presets[index].parameters = parameters
        saveConfig()
    }

    private fun loadPreset(preset: Preset) {
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Loading preset ${preset.name}")
        for (parameter in preset.parameters) {
            OSCSender.send(OSCMessage(parameter.key, listOf(parameter.value.any())))
        }
    }

    @Composable
    override fun onSettingsUI() {
        val curAvatarId by remember { CoreModules.GameStorage.curAvatarId }

        val saveLoadScope = rememberCoroutineScope()

        if (curAvatarId === null) {
            Text(
                "Loading your avatar...",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.h6
            )
            return
        }

        if (config!!.avatars[curAvatarId] === null) {
            config!!.avatars[curAvatarId!!] = Avatar(curAvatarId!!)
        }

        var curAvatarName by remember { mutableStateOf(config!!.avatars[curAvatarId]!!.name) }

        TextField(
            value = curAvatarName,
            onValueChange = {
                config!!.avatars[curAvatarId]!!.name = it
                curAvatarName = it
                saveConfig()
            },
            label = { Text("Avatar Name") }
        )

        val presets = config!!.avatars[curAvatarId]!!.presets.toMutableStateList()

        for (preset in presets) {
            var presetName by remember { mutableStateOf(preset.name) }
            var enabled by remember { mutableStateOf(false) }

            Button(
                onClick = { enabled = !enabled }
            ) {
                Text(presetName)
            }
            AnimatedVisibility(visible = enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colors.background) // TODO: custom color for secondary background
                        .border(1.dp, MaterialTheme.colors.onPrimary, shape = RoundedCornerShape(5.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { enabled = !enabled }
                        ) {
                            Text(presetName)
                        }
                        TextField(
                            value = presetName,
                            onValueChange = {
                                presetName = it
                                preset.name = it
                                config!!.avatars[curAvatarId]!!.presets[presets.indexOf(preset)].name = it
                                saveConfig()
                            },
                            label = { Text("Preset Name") }
                        )

                        Button(
                            onClick = {
                                saveLoadScope.launch {
                                    savePreset(config!!.avatars[curAvatarId]!!.presets.indexOf(preset))
                                }
                            }
                        ) {
                            Text("Save Preset")
                        }

                        Button(
                            onClick = {
                                saveLoadScope.launch {
                                    loadPreset(preset)
                                }
                            }
                        ) {
                            Text("Load Preset")
                        }

                        Button(
                            onClick = {
                                presets.remove(preset)
                                config!!.avatars[curAvatarId]!!.presets.remove(preset)
                                saveConfig()
                            }
                        ) {
                            Text("Delete Preset")
                        }
                    }
                }
            }
        }

        Button(
            onClick =
            {
                val preset = Preset("Preset ${presets.size + 1}")
                config!!.avatars[curAvatarId]!!.presets.add(preset)
                presets.add(preset)
                saveConfig()
                saveLoadScope.launch {
                    savePreset(presets.indexOf(preset))
                }
            }
        )
        {
            Text("New Preset")
        }
    }
}