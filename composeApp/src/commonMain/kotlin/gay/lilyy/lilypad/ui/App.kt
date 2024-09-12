package gay.lilyy.lilypad.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

        // Scrollable

        val scrollState = rememberScrollState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(scrollState)
                .fillMaxWidth()
        ) {
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