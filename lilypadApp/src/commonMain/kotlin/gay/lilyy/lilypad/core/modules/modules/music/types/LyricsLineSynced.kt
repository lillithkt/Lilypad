package gay.lilyy.lilypad.core.modules.modules.music.types

import kotlinx.serialization.Serializable

@Serializable
data class LyricsLineSyncedLine(
    val opposite: Boolean,
    val start: Float,
    val end: Float,
    val text: String
)

@Serializable
data class LyricsLineSynced(
    val type: SyncType = SyncType.LINE_SYNCED,
    val lines: List<LyricsLineSyncedLine>,
)