package gay.lilyy.lilypad.core

import gay.lilyy.lilypad.core.osc.OSCQuery
import gay.lilyy.lilypad.core.osc.OSCReceiver
import gay.lilyy.lilypad.core.osc.OSCSender
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun startCore() {
    Napier.base(DebugAntilog())


    // Initialize OSC components
    OSCQuery
    OSCReceiver
    OSCSender

    while (true) {
        tick()
        Thread.sleep(1000)
    }
}

fun tick() {
//    println("Tick!")
}