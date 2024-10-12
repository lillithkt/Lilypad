package gay.lilyy.lilypad.core.modules.modules.message

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import com.illposed.osc.OSCMessage
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import gay.lilyy.lilypad.core.osc.OSCSender
import gay.lilyy.lilypad.ui.components.LText

@Suppress("unused")
class Message : ChatboxModule<Any>() {
    override val name = "Message"

    var previousMessage: MutableState<String?> = mutableStateOf(null)
    var lastMessage: MutableState<String> = mutableStateOf("")
    var lastMessageTime: Long = 0
    var previousMessageTime: Long = 0
    var lastTypingPing: Long = 0

    override val hasSettingsUI = true

    fun sendTypingMessage() {
        // send the current last message
        OSCSender.send(OSCMessage("/chatbox/input", listOf(lastMessage.value, true)))
        previousMessage.value = lastMessage.value
        lastMessage.value = ""
        lastMessageTime = System.currentTimeMillis()
        previousMessageTime = System.currentTimeMillis()
        OSCSender.send(OSCMessage("/chatbox/typing", listOf(false)))
    }

    @Composable
    override fun onSettingsUI() {
        val previousMessage by remember { previousMessage }
        var lastMessage by remember { lastMessage }

        // button to send previous message
        if (previousMessage != null) {
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
            onValueChange = { lastMessage = it
                            lastMessageTime = System.currentTimeMillis()},
            label = { Text("Type a message") }
        )

        Button(
            onClick = {
                sendTypingMessage()
            }
        ) {
            Text("Send")
        }


    }

    override val chatboxBuildOrder = 1
    override fun buildChatbox(): List<String?>? {
        // I am using this as a basic tick function because im lazy

        // Check if it has been 15s since the last message
        if (System.currentTimeMillis() - lastMessageTime > 15000) {
            lastMessage.value = ""
            lastMessageTime = System.currentTimeMillis()
        }

        if (lastMessage.value.isNotEmpty()) {
            // If lastTypingPing is over 5s ago, send a typing message
            if (System.currentTimeMillis() - lastTypingPing > 5000) {
                OSCSender.send(OSCMessage("/chatbox/typing", listOf(true)))
                lastTypingPing = System.currentTimeMillis()
            }
        }
        return null
    }
    override fun buildFullChatbox(): List<String?>? {
        if (lastMessage.value.isNotEmpty()) {
            return listOf(lastMessage.value)
        }
        if (previousMessage.value != null && System.currentTimeMillis() - previousMessageTime < 15000) {
            return listOf(previousMessage.value)
        }
        return null
    }
}