package gay.lilyy.lilypad.core.modules.modules.spotify.types

import kotlinx.serialization.Serializable

@Serializable
data class LyricsLineSyncedLine(
    val opposite: Boolean,
    val start: Int,
    val end: Int,
    val text: String
)

@Serializable
data class LyricsLineSynced(
    val type: SyncType = SyncType.LINE_SYNCED,
    val lines: List<LyricsLineSyncedLine>,
)