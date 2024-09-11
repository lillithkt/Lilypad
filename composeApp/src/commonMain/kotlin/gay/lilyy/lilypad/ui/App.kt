package gay.lilyy.lilypad.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.startCore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    scope.launch(Dispatchers.Default) {
        startCore()
    }
    MaterialTheme {

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Settings", style = MaterialTheme.typography.h4)

            for (module in Modules.modules.values) {
                if (module.hasSettingsUI) {
                    // Collapsible
                    var showContent by remember { mutableStateOf(false) }
                    Button(onClick = { showContent = !showContent }) {
                        Text(module.name)
                    }
                    if (showContent) {
                        module.onSettingsUI()
                    }
                }
            }
        }
    }
}