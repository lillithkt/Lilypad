package gay.lilyy.lilypad.core.modules.modules.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyAuthConfig(
    var clientId: String = "",
    var clientSecret: String = "",
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
    var enabled: Boolean = false,
    var updateInterval: Int = 5000,
    val lyrics: SpotifyLyricsConfig = SpotifyLyricsConfig(),
    var auth: SpotifyAuthConfig = SpotifyAuthConfig(),
    var nonAuth: SpotifyNonAuthConfig = SpotifyNonAuthConfig(),
    var useAuthConfig: Boolean = false,
    var spotubeIntegration: Boolean = false
)
