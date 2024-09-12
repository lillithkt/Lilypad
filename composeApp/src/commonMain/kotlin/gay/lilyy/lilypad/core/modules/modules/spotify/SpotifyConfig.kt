package gay.lilyy.lilypad.core.modules.modules.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyAuthConfig(
    var clientId: String = "",
    var clientSecret: String = "",
    @SerialName("Token Explanation")
    val _tokenExplanation: String = "Do not specify the token yourself. It will automatically be regenerated",
    var token: String? = null
)

@Serializable
data class SpotifyNonAuthConfig(
    val endpoint: String = ""
)

@Serializable
data class SpotifyLyricsConfig(
    var enabled: Boolean = true,
    @SerialName("Provider Explanation")
    val _providerExplanation: String = "The provider to use for lyrics. This is an instance of https://github.com/imlvna/spotify-lyrics-api",
    var provider: String = "https://spotify-lyrics-api.lvna.workers.dev"
)

@Serializable
data class SpotifyConfig(
    var enabled: Boolean = false,
    var updateInterval: Int = 5000,
    val lyrics: SpotifyLyricsConfig = SpotifyLyricsConfig(),
    var auth: SpotifyAuthConfig = SpotifyAuthConfig(),
    var nonAuth: SpotifyNonAuthConfig = SpotifyNonAuthConfig(),
    @SerialName("Use Auth Config Explanation")
    val _useAuthConfigExplanation: String = "If enabled, the app will use the auth config. If disabled, the app will use the non-auth config. Auth config is standard oauth, non-auth config is an endpoint configured to return the spotify listening data with no authentication, such as https://github.com/ImLvna/spotify-listening",
    var useAuthConfig: Boolean = false
)
