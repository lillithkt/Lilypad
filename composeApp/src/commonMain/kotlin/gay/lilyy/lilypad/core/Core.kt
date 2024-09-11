package gay.lilyy.lilypad.core

import gay.lilyy.lilypad.core.osc.OSCQuery
import gay.lilyy.lilypad.core.osc.OSCReceiver
import gay.lilyy.lilypad.core.osc.OSCSender

fun startCore() {
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