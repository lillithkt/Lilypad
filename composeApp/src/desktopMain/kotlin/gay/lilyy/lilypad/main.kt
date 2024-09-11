package gay.lilyy.lilypad

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import gay.lilyy.lilypad.config.ConfigStorage
import gay.lilyy.lilypad.core.startCore
import gay.lilyy.lilypad.ui.App

fun main() {
    registerPreCloseListener()
    if (ConfigStorage.all.core.headless) {
        println("Lilypad is running in headless mode.")
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