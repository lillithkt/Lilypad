package gay.lilyy.lilypad.core.modules.modules.music

import kotlinx.serialization.Serializable

@Serializable
data class LastFMConfig(
    var apiKey: String? = null,
    var username: String? = null,
)
