package gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox

import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.illposed.osc.OSCMessage
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.osc.OSCSender
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val maxLines = 9
const val maxChars = 144

@Suppress("unused")
class Chatbox : ChatboxModule<ChatboxConfig>() {
    override val name = "Chatbox"

    override val configClass = ChatboxConfig::class

    var lastOutput: SnapshotStateList<String?> = mutableStateListOf()
    private var timeoutUp: Boolean = true

    private fun build(): List<String> {
        if (!config!!.enabled) return emptyList()
        val modules = Modules.modules.values.filterIsInstance<ChatboxModule<*>>().sortedBy { it.chatboxBuildOrder }

        val outputs = mutableListOf<String>()

        for (module in modules) {
            outputs += module.buildChatbox()?.filterNotNull() ?: continue
        }

        val lines: MutableList<String> = mutableListOf()

        for (output in outputs) {
            // You are limited to 9 lines and 144 characters total. trimByModule will not add that module to the chatbox if it pushes you over the limit. trimByLine will not add any extra lines that go over the limit, but will include the rest of the module
            if (config!!.trimByLine) {
                if (lines.size + output.lines().size > maxLines || lines.sumOf { it.length } + output.length > maxChars) {
                    val remaining = maxLines - lines.size
                    lines += output.lines().take(remaining)
                    break
                }
            } else if (config!!.trimByModule) {
                if (lines.size + output.lines().size > maxLines || lines.sumOf { it.length } + output.length > maxChars) {
                    break
                }
            }

            lines += output
        }

        return lines
    }

    private fun resetTimeout() {
        timeoutUp = false
        Thread {
            Thread.sleep(config!!.updateInterval.toLong())
            timeoutUp = true
        }.start()
    }

    private suspend fun loopBuildChatbox() {
        while (true) {
            val output = build()
            withContext(Dispatchers.Main) {
                if (output != lastOutput) {
                    if (timeoutUp) {
                        val chatbox = output.joinToString("\n")
                        if (CoreModules.Core.config!!.logs.outgoingChatbox) Napier.v(chatbox)
                        OSCSender.send(OSCMessage("/chatbox/input", listOf(chatbox, true, false)))
                        val scope = CoroutineScope(Dispatchers.Main)
                        scope.launch {
                            lastOutput.clear()
                            lastOutput.addAll(output)
                        }
                        resetTimeout()
                    }
                }
            }
            withContext(Dispatchers.IO) {
                Thread.sleep(250)
            }
        }
    }

    fun clearChatbox(onlyIfDisabled: Boolean = false) {
        if (onlyIfDisabled && config!!.enabled) return
        if (CoreModules.Core.config!!.logs.outgoingChatbox) Napier.v("Clearing chatbox")
        OSCSender.send(
            OSCMessage(
                "/chatbox/input", listOf(
                    "",
                    true,
                    false
                )
            )
        )
        // Sometimes vrchat just ignores the empty message, so we send it again
        Thread {
            Thread.sleep(250)
            if (onlyIfDisabled && config!!.enabled) return@Thread
            OSCSender.send(
                OSCMessage(
                    "/chatbox/input", listOf(
                        "",
                        true,
                        false
                    )
                )
            )
        }.start()
    }

    override val hasSettingsUI = true

    @Composable
    override fun onSettingsUI() {
        var enabled by remember { mutableStateOf(config!!.enabled) }
        var updateInterval by remember { mutableStateOf(config!!.updateInterval) }

        Text("Enabled")
        Checkbox(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                config!!.enabled = it
                if (!it) {
                    clearChatbox(true)
                }
                saveConfig()
            },
        )

        Text("Update Interval")
        TextField(
            value = updateInterval.toString(),
            onValueChange = { updateInterval = it.toIntOrNull() ?: 0 },
        )
    }

    override fun init() {
        super.init()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            loopBuildChatbox()
        }
    }
}