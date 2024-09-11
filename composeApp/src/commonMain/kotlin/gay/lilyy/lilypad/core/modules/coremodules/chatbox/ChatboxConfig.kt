package gay.lilyy.lilypad.core.modules.coremodules.chatbox

import gay.lilyy.lilypad.core.modules.modules.spotify.SpotifyConfig
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
