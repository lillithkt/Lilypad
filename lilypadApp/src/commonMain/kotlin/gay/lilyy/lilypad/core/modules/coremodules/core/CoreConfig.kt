package gay.lilyy.lilypad.core.CoreModules.CoreCoreModules.Core

import kotlinx.serialization.Serializable

@Serializable
data class ServerLogsConfig(
    var incomingData: Boolean = false,
    var outgoingData: Boolean = false,
    var outgoingChatbox: Boolean = false,
    var errors: Boolean = true,
    var warnings: Boolean = true,
    var debug: Boolean = false
)


@Serializable
data class CoreConfig(
    val headless: Boolean = false,
    var listen: Int = 7232,
    var oscQuery: Boolean = true,
    var connect: String = "localhost:9000",
    val logs: ServerLogsConfig = ServerLogsConfig()
)
