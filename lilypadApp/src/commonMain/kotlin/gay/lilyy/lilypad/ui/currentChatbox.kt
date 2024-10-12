package gay.lilyy.lilypad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gay.lilyy.lilypad.core.modules.CoreModules

@Composable
fun currentChatbox() {
    var chatboxText by remember { mutableStateOf("") }

    LaunchedEffect(CoreModules.Chatbox.lastOutput) {
        snapshotFlow { CoreModules.Chatbox.lastOutput.toList() }
            .collect { output ->
                chatboxText = output.filterNotNull().joinToString("\n")
            }
    }


    if (chatboxText.isNotEmpty()) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colors.background)
                .border(1.dp, MaterialTheme.colors.onBackground, shape = RoundedCornerShape(5.dp))
                .padding(8.dp)
        ) {

            Text(
                chatboxText, style = MaterialTheme.typography.body1.merge(
                    // Center text
                    TextStyle(textAlign = TextAlign.Center)
                )
            )
        }
    }
}