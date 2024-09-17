package gay.lilyy.lilypad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.gamestorage.GameStorage
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

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lilypad", style = MaterialTheme.typography.h3)

            val gs = Modules.get<GameStorage>("GameStorage")!!
            val curUsername by remember { gs.curUsername }
            if (curUsername != null) {
                Text("Welcome, $curUsername", style = MaterialTheme.typography.h6)
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
                    .height(256.dp)
            ) {

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(MaterialTheme.colors.primary)
                        .border(1.dp, MaterialTheme.colors.onPrimary, shape = RoundedCornerShape(5.dp))
                ) {
                    CurrentChatbox()
                }
            }
        }



        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 64.dp)
                .verticalScroll(scrollState)
                .fillMaxWidth()
        ) {
            Text("Settings", style = MaterialTheme.typography.h4)

            for (module in Modules.modules.values) {
                if (module.hasSettingsUI && !module.disabled) {
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