package gay.lilyy.lilypad.core.modules.coremodules.core

import kotlinx.serialization.SerialName
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
    @SerialName("Module Packages Explanation")
    val _modulePackagesExplanation: String = "A list of packages to scan for modules. Core modules are hardcoded into the resolver, and do not need to be added here.",
    val modulePackages: List<String> = listOf("gay.lilyy.lilypad.core.modules.modules"),
    val logs: ServerLogsConfig = ServerLogsConfig()
)
