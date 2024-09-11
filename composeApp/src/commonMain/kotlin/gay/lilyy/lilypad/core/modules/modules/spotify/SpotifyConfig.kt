package gay.lilyy.lilypad.core.modules.modules.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyAuthConfig(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "",
    val refreshToken: String = ""
)

@Serializable
data class SpotifyNonAuthConfig(
    val endpoint: String = ""
)

@Serializable
data class SpotifyLyricsConfig(
    val enabled: Boolean = true,
    @SerialName("Provider Explanation")
    val _providerExplanation: String = "The provider to use for lyrics. This is an instance of https://github.com/imlvna/spotify-lyrics-api",
    val provider: String = "https://spotify-lyrics-api.lvna.workers.dev"
)

@Serializable
data class SpotifyConfig(
    val enabled: Boolean = false,
    val lyrics: SpotifyLyricsConfig = SpotifyLyricsConfig(),
    val auth: SpotifyAuthConfig? = SpotifyAuthConfig(),
    val nonAuth: SpotifyNonAuthConfig? = SpotifyNonAuthConfig(),
    @SerialName("Use Auth Config Explanation")
    val _useAuthConfigExplanation: String = "If enabled, the app will use the auth config. If disabled, the app will use the non-auth config. Auth config is standard oauth, non-auth config is an endpoint configured to return the spotify listening data with no authentication, such as https://github.com/ImLvna/spotify-listening",
    val useAuthConfig: Boolean = false
)
