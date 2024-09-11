package gay.lilyy.lilypad.core.modules.coremodules.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerLogsConfig(
    val incomingData: Boolean = false,
    val outgoingData: Boolean = false,
    val outgoingChatbox: Boolean = false,
    val errors: Boolean = true
)

@Serializable
data class CoreConfig(
    val headless: Boolean = false,
    var listen: Int = 7232,
    var connect: String = "localhost:9000",
    @SerialName("Module Packages Explanation")
    val _modulePackagesExplanation: String = "A list of packages to scan for modules. Core modules are hardcoded into the resolver, and do not need to be added here.",
    val modulePackages: List<String> = listOf("gay.lilyy.lilypad.core.modules.modules"),
    val logs: ServerLogsConfig = ServerLogsConfig()
)
