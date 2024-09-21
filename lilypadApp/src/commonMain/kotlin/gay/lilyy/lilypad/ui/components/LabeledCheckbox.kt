package gay.lilyy.lilypad.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun LabeledCheckbox(
    label: String = "",
    labelModifier: Modifier = Modifier,
    labelStyle: TextStyle = LocalTextStyle.current,
    customLabel: @Composable () -> Unit = { Text(label, style = labelStyle, modifier = labelModifier) },
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    checkboxModifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    Row(verticalAlignment = CenterVertically) {
        customLabel()
        val userOnCheckedChange = onCheckedChange ?: {}
        Checkbox(
            checked = checked,
            onCheckedChange = userOnCheckedChange,
            modifier = checkboxModifier,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = colors
        )
    }
}