package gay.lilyy.lilypad.core.modules.modules.music

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyAuthConfig(
    var clientId: String = "c3f24d18504a41d9ae10d01afa8f91dc",
    var clientSecret: String = "a9439ea221b64e49995d34ece0f47d40",
    @SerialName("Token Explanation")
    val _tokenExplanation: String = "Do not specify the token yourself. It will automatically be regenerated",
    var token: String? = null,
    var refreshToken: String? = null,
)

@Serializable
data class SpotifyNonAuthConfig(
    val endpoint: String = ""
)

@Serializable
data class SpotifyLyricsConfig(
    var enabled: Boolean = true,
    var provider: String = "https://spotify-lyrics-api.lvna.workers.dev"
)

@Serializable
data class SpotifyConfig(
    val lyrics: SpotifyLyricsConfig = SpotifyLyricsConfig(),
    var auth: SpotifyAuthConfig = SpotifyAuthConfig(),
    var nonAuth: SpotifyNonAuthConfig = SpotifyNonAuthConfig(),
    var useAuthConfig: Boolean = false,
    var showTimestamp: Boolean = true,
)
