package gay.lilyy.lilypad.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.Chatbox
import gay.lilyy.lilypad.core.modules.CoreModules
import io.github.aakira.napier.Napier

@Composable
fun CurrentChatbox() {
    var chatboxText by remember { mutableStateOf("") }

    LaunchedEffect(CoreModules.Chatbox.lastOutput) {
                snapshotFlow { CoreModules.Chatbox.lastOutput.toList() }
                    .collect { output ->
                chatboxText = output.filterNotNull().joinToString("\n")
            }
    }

    Text(chatboxText, style = MaterialTheme.typography.body1.merge(
        // Center text
        TextStyle(textAlign = TextAlign.Center)
    ))
}