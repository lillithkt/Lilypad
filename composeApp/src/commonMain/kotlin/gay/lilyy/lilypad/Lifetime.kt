package gay.lilyy.lilypad

import gay.lilyy.lilypad.core.modules.ConfigStorage
import gay.lilyy.lilypad.core.modules.Modules

fun registerPreCloseListener() {
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Lilypad is shutting down. Saving data...")
        for (module in Modules.modules.values) {
            module.saveConfig(write = false)
        }
        ConfigStorage.save()
    })
}