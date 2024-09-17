package gay.lilyy.lilypad.core

import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

object HTTPServer {
    val locks: MutableList<String> = mutableListOf()

    val server: ApplicationEngine = embeddedServer(Netty, port = Constants.HTTP_PORT) {
        routing {
            for (module in Modules.modules.values) {
                if (module.httpServerRouting(this)) {
                    if (Modules.Core.config!!.logs.debug) Napier.d("Registered routing for module ${module.name}")
                }
            }
        }
    }

    fun lock(lock: String) {
        if (hasLock(lock)) return
        locks.add(lock)
        if (locks.size == 1) {
            server.start(wait = false)
        }
    }

    fun unlock(lock: String) {
        if (!hasLock(lock)) return
        locks.remove(lock)
        if (locks.isEmpty()) {
            server.stop(1000, 1000)
        }
    }

    fun hasLock(lock: String): Boolean {
        return locks.contains(lock)
    }
}