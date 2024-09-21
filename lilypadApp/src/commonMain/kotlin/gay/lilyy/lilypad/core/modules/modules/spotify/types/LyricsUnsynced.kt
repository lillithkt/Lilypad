package gay.lilyy.lilypad.core.modules.modules.spotify.types

import kotlinx.serialization.Serializable

@Serializable
data class LyricsUnsyncedLine(
    val opposite: Boolean,
    val text: String,
)

@Serializable
data class LyricsUnsynced(
    val type: SyncType = SyncType.UNSYNCED,
    val lines: List<LyricsUnsyncedLine>,
)