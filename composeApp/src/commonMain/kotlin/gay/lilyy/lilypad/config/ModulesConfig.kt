package gay.lilyy.lilypad.config

import gay.lilyy.lilypad.config.chatbox.SpotifyConfig
import kotlinx.serialization.Serializable

@Serializable
data class ModulesConfig(
    val spotify: SpotifyConfig = SpotifyConfig(),
)
