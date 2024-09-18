package gay.lilyy.lilypad.core.modules.modules.spotify.types

import kotlinx.serialization.Serializable

@Serializable
data class SyllableLyricGroup(
    val words: String,
    val part: Boolean,
    val start: Float,
    val end: Float
)

@Serializable
data class Background(
    val groups: List<SyllableLyricGroup>,
    val start: Float,
    val end: Float
)

@Serializable
data class LyricsSyllableSyncedLine(
    val opposite: Boolean,
    val start: Float,
    val end: Float,
    val lead: List<SyllableLyricGroup>? = null,
    val background: Background? = null
)

@Serializable
data class LyricsSyllableSynced(
    val type: SyncType = SyncType.SYLLABLE_SYNCED,
    val lines: List<LyricsSyllableSyncedLine>,
)