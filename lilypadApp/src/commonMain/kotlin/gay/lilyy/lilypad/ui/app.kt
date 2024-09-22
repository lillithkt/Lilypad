package gay.lilyy.lilypad.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules

import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun app() {
    MaterialTheme {

        // Scrollable

        val scrollState = rememberScrollState()

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lilypad", style = MaterialTheme.typography.h3)
            var curUsername by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(CoreModules.GameStorage.curUsername) {
                snapshotFlow { CoreModules.GameStorage.curUsername.value }
                    .collect { username ->
                        curUsername = username
                    }
            }
            if (curUsername != null) {
                Text("Welcome, $curUsername", style = MaterialTheme.typography.h6)
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
                    .height(256.dp)
            ) {
                currentChatbox()
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

            var openedModule by remember { mutableStateOf<String?>(null) }

            for (module in Modules.modules.values) {
                if (module.hasSettingsUI && !module.disabled) {
                    Button(onClick = { openedModule = if (openedModule == module.name) null else module.name }) {
                        Text(module.name)
                    }

                    AnimatedVisibility(visible = openedModule == module.name) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colors.background)
                                .border(1.dp, MaterialTheme.colors.onPrimary, shape = RoundedCornerShape(5.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                module.onSettingsUI()
                            }
                        }
                    }
                }
            }
        }
    }
}