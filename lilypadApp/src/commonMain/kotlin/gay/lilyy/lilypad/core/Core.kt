package gay.lilyy.lilypad.core

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun globalInit() {
    Napier.base(DebugAntilog())
}