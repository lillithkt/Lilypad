package gay.lilyy.lilypad.core.modules.modules.message

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import com.illposed.osc.OSCMessage
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxFlags
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import gay.lilyy.lilypad.core.osc.OSCSender
import gay.lilyy.lilypad.getPlatform
import gay.lilyy.lilypad.ui.components.LText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("unused")
class Message : ChatboxModule<Any>() {
    override val name = "Message"

    override val disabled = getPlatform().name.contains("Android")

    private var previousMessage: MutableState<String?> = mutableStateOf(null)
    private var lastMessageState: MutableState<String> = mutableStateOf("")
    private var lastMessageTime: Long = 0
    private var previousMessageTime: Long = 0
    private var lastTypingPing: Long = 0

    override val hasSettingsUI = true

    private fun sendTypingMessage() {
        // send the current last message
        if (lastMessageState.value.isNotEmpty())
            OSCSender.send(OSCMessage("/chatbox/input", listOf(lastMessageState.value, true)))
        previousMessage.value = lastMessageState.value
        lastMessageState.value = ""
        lastMessageTime = System.currentTimeMillis()
        previousMessageTime = System.currentTimeMillis()
        OSCSender.send(OSCMessage("/chatbox/typing", listOf(false)))
    }

    @Composable
    override fun onSettingsUI() {
        val previousMessage by remember { previousMessage }
        var lastMessage by remember { lastMessageState }

        // button to send previous message
        if (!previousMessage.isNullOrEmpty()) {
            Text("Previous Message")
            LText.Caption(previousMessage!!)
            Button(onClick = {
                lastMessage = previousMessage!!
                lastMessageTime = System.currentTimeMillis()
                sendTypingMessage()
            }) {
                Text("Send Previous Message")
            }
        }

        // button to send custom message
        TextField(
            value = lastMessage,
            onValueChange = { value ->
                lastMessage = value.filter { it != '\n' }
                lastMessageTime = System.currentTimeMillis()},
            modifier = Modifier.onKeyEvent { keyEvent ->
                if (listOf(KeyEventType.KeyDown, KeyEventType.KeyUp).contains(keyEvent.type) && keyEvent.key == Key.Enter) {
                    sendTypingMessage()
                    true
                } else {
                    false
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    sendTypingMessage()
                }
            ),
            label = { Text("Type a message") }
        )
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    override val chatboxBuildOrder = 1
    override fun buildChatbox(): List<String?>? {
        // I am using this as a basic tick function because im lazy
        scope.launch {
            // Check if it has been 15s since the last message
            if (System.currentTimeMillis() - lastMessageTime > 15000) {
                lastMessageState.value = ""
                lastMessageTime = System.currentTimeMillis()

            }


            if (lastMessageState.value.isNotEmpty()) {
                // If lastTypingPing is over 5s ago, send a typing message
                if (System.currentTimeMillis() - lastTypingPing > 5000) {
                    OSCSender.send(OSCMessage("/chatbox/typing", listOf(true)))
                    lastTypingPing = System.currentTimeMillis()
                }
            }
        }
        return null
    }
    override fun buildFullChatbox(): List<String?>? {
        // FIX THIS COPILOT: Reading a state that was created after the snapshot was taken or in a snapshot that has not yet been applied
        if (lastMessageState.value.isNotEmpty()) {
            return listOf(ChatboxFlags.NONTRANSPARENT.flag, lastMessageState.value)
        }
        if (!previousMessage.value.isNullOrEmpty() && System.currentTimeMillis() - previousMessageTime < 15000) {
            return listOf(ChatboxFlags.NONTRANSPARENT.flag, previousMessage.value)
        }
        return null
    }
}