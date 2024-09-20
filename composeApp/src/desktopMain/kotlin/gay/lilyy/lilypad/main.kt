package gay.lilyy.lilypad

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import gay.lilyy.lilypad.core.globalInit
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.startCore
import gay.lilyy.lilypad.ui.App
import io.github.aakira.napier.Napier

fun main() {
    globalInit()
    registerPreCloseListener()
    if (CoreModules.Core.config!!.headless) {
        Napier.i("Lilypad is running in headless mode.")
        startCore()
    } else
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Lilypad",
                icon = BitmapPainter(useResource("icons/icon.png", ::loadImageBitmap)),
            ) {
                App()
            }
        }
}