package gay.lilyy.lilypad.config.chatbox

import kotlinx.serialization.Serializable

@Serializable
data class ChatboxModulesConfig(
    val spotify: SpotifyConfig = SpotifyConfig(),
)

@Serializable
data class ChatboxConfig(
    val enabled: Boolean = true,
    val updateInterval: Int = 2000,

    val modules: ChatboxModulesConfig = ChatboxModulesConfig()
)
