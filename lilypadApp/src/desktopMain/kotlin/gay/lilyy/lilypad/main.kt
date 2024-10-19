package gay.lilyy.lilypad

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import gay.lilyy.lilypad.core.globalInit
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.ui.app
import gay.lilyy.lilypad.util.PrintStreamDuplexer
import io.github.aakira.napier.Napier
import java.io.File
import java.io.PrintStream

fun main() {
    val logFile = File(getFilesDir(), "lilypad.log")
    val logStream = PrintStreamDuplexer(System.out, PrintStream(logFile))
    System.setOut(logStream)
    System.setErr(logStream)
    globalInit()
    registerPreCloseListener()
    if (CoreModules.Core.config!!.headless) {
        Napier.i("Lilypad is running in headless mode.")
        // No further initialization is needed.
        // getting CoreModules initializes Modules,
        // which in turn initialize all osc stuff when needed
    } else
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Lilypad",
                icon = BitmapPainter(useResource("icons/icon.png", ::loadImageBitmap)),
            ) {
                app()
            }
        }
}