package gay.lilyy.lilypad.core.modules.modules.spotify.spotube

import com.adamratzman.spotify.models.Track
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotubeExternalUrls(
    val spotify: String? = null
)

@Serializable
data class SpotubeArtist(
    @SerialName("external_urls")
    val externalUrls: SpotubeExternalUrls,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

// WARNING: This class is not complete
// I am only adding what I need, and enabling ignoreUnknownKeys
@Serializable
data class SpotubeTrack(
    val artists: List<SpotubeArtist>,
    val href: String,
    val id: String,
    val name: String
) {
    companion object {
        fun fromTrack(track: Track): SpotubeTrack {
            return SpotubeTrack(
                artists = track.artists.map {
                    SpotubeArtist(
                        externalUrls = SpotubeExternalUrls(it.externalUrls.spotify),
                        href = it.href,
                        id = it.id,
                        name = it.name ?: "Unknown Name",
                        type = it.type,
                        uri = it.uri.toString()
                    )
                },
                href = track.href,
                id = track.id,
                name = track.name
            )
        }
    }
}