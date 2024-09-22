package gay.lilyy.lilypad.core.modules.modules.spotify.spotube

import gay.lilyy.lilypad.core.modules.CoreModules
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable

@Serializable
data class BaseWsEvent(
    val type: String
)

enum class WsEvents(val type: String, val handler: (String, String) -> Unit)
{
    QUEUE("queue", { json, address ->
        val event = Spotube.json.decodeFromString(QueueEvent.serializer(), json)
        Spotube.clients[address]!!.currentTrack = event.data.playlist.medias[event.data.playlist.index].extras.track
        Spotube.clients[address]!!.playing = event.data.playing
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Current Spotube track: ${Spotube.clients[address]!!.currentTrack?.name}; playing: ${event.data.playing}")
    }),
    POSITION("position", { json, address ->
        val event = Spotube.json.decodeFromString(PositionEvent.serializer(), json)
        Spotube.clients[address]!!.currentTrackPosition = event.data * 1000
    }),
    PLAYING("playing", { json, address ->
        val event = Spotube.json.decodeFromString(PlayingEvent.serializer(), json)
        Spotube.clients[address]!!.playing = event.data
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Spotube Playing: ${event.data}")
    })
}

@Serializable
data class QueueEventExtras(
    val track: SpotubeTrack
)

@Serializable
data class QueueEventMedia(
    val extras: QueueEventExtras
)

@Serializable
data class QueueEventPlaylist(
    val index: Int = 0,
    val medias: List<QueueEventMedia> = listOf()
)

@Serializable
data class QueueEventData(
    val playing: Boolean = false,
    val playlist: QueueEventPlaylist = QueueEventPlaylist()
)

@Serializable
data class QueueEvent(
    val type: String = "queue",
    val data: QueueEventData
)

@Serializable
data class PositionEvent(
    val type: String = "position",
    val data: Int
)

@Serializable
data class PlayingEvent(
    val type: String = "playing",
    val data: Boolean
)