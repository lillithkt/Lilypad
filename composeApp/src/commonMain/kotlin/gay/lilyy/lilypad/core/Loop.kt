package gay.lilyy.lilypad.core

import gay.lilyy.lilypad.core.modules.Modules

fun startCore() {

    while (true) {
        tick()
        Thread.sleep(1000)
    }
}

fun tick() {
    println("Tick!")
}