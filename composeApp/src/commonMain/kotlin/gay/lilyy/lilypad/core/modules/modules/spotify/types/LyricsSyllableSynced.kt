package gay.lilyy.lilypad.core.modules.modules.spotify.types

import kotlinx.serialization.Serializable

@Serializable
data class SyllableLyricGroup(
    val words: String,
    val part: Boolean,
    val start: Int,
    val end: Int
)

@Serializable
data class Background(
    val groups: List<SyllableLyricGroup>,
    val start: Int,
    val end: Int
)

@Serializable
data class LyricsSyllableSyncedLine(
    val opposite: Boolean,
    val start: Int,
    val end: Int,
    val lead: List<SyllableLyricGroup>?,
    val background: Background?
)

@Serializable
data class LyricsSyllableSynced(
    val type: SyncType = SyncType.SYLLABLE_SYNCED,
    val lines: List<LyricsSyllableSyncedLine>,
)