package gay.lilyy.lilypad

import com.illposed.osc.OSCMessage
import gay.lilyy.lilypad.core.modules.ConfigStorage
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.osc.OSCSender
import io.github.aakira.napier.Napier

fun registerPreCloseListener() {
    Runtime.getRuntime().addShutdownHook(Thread {
        if (CoreModules.Core.config!!.logs.debug) Napier.v("Lilypad is shutting down. Saving data...")
        for (module in Modules.modules.values) {
            module.saveConfig(write = false)
        }
        OSCSender.send(OSCMessage("/chatbox/input", listOf<Any>()))
        ConfigStorage.save()
    })
}