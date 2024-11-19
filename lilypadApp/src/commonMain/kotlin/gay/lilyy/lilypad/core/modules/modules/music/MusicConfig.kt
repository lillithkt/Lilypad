package gay.lilyy.lilypad.core.modules.modules.music

import kotlinx.serialization.Serializable

@Serializable
enum class MusicType(val type: String) {
    SPOTIFY("spotify"),
    LASTFM("lastfm"),
}

@Serializable
data class MusicConfig(
    var enabled: Boolean = false,
    var updateInterval: Int = 5000,
    var showArtist: Boolean = true,
    var type: MusicType = MusicType.SPOTIFY,
    var spotify: SpotifyConfig = SpotifyConfig(),
    var lastFM: LastFMConfig = LastFMConfig(),
)
