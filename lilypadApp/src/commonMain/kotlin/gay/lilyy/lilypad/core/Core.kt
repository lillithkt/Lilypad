package gay.lilyy.lilypad.core

import gay.lilyy.lilypad.getFilesDir
import gay.lilyy.lilypad.util.PrintStreamDuplexer
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.io.File
import java.io.PrintStream

fun globalInit() {
    val logFile = File(getFilesDir(), "lilypad.log")
    val logStream = PrintStreamDuplexer(System.out, PrintStream(logFile))
    System.setOut(logStream)
    System.setErr(logStream)
    Napier.base(DebugAntilog())
}