package gay.lilyy.lilypad

import gay.lilyy.lilypad.core.modules.ConfigStorage
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier

fun registerPreCloseListener() {
    Runtime.getRuntime().addShutdownHook(Thread {
        CoreModules.Chatbox.clearChatbox()
        if (CoreModules.Core.config!!.logs.debug) Napier.v("Lilypad is shutting down. Saving data...")
        for (module in Modules.modules.values) {
            module.saveConfig(write = false)
        }
        ConfigStorage.save()
    })
}