package gay.lilyy.lilypad.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.chatbox.Chatbox
import io.github.aakira.napier.Napier

@Composable
fun CurrentChatbox() {
    val chatboxModule = Modules.get<Chatbox>("Chatbox")
    if (chatboxModule == null) {
        Napier.e("Chatbox module not found")
        Text("Chatbox module not found", color = MaterialTheme.colors.error, style = MaterialTheme.typography.h6)
        return
    }

    // Safely observe lastOutput changes using snapshotFlow
    var chatboxText by remember { mutableStateOf("") }

    LaunchedEffect(chatboxModule.lastOutput) {
        snapshotFlow { chatboxModule.lastOutput.toList() }
            .collect { output ->
                chatboxText = output.filterNotNull().joinToString("\n")
            }
    }

    Text(chatboxText, style = MaterialTheme.typography.h6.merge(
        // Center text
        TextStyle(textAlign = TextAlign.Center)
    ))
}