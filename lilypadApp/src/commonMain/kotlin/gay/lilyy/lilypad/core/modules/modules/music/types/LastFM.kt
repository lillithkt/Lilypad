package gay.lilyy.lilypad.core.modules.modules.music.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastFMArtist(
    @SerialName("#text")
    val name: String,
)
@Serializable
data class LastFMTrackAttr(
    @SerialName("nowplaying")
    val nowPlaying: Boolean,
)
@Serializable
data class LastFMTrack(
    val artist: LastFMArtist,
    val name: String,
    @SerialName("@attr")
    val attr: LastFMTrackAttr,
)
