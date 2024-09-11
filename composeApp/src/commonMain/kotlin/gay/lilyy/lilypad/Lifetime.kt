package gay.lilyy.lilypad

import gay.lilyy.lilypad.config.ConfigStorage

fun registerPreCloseListener() {
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Lilypad is shutting down. Saving data...")
        ConfigStorage.save()
    })
}