package gay.lilyy.lilypad.core.modules.modules.spotify.types

enum class SyncType {
    UNSYNCED,
    LINE_SYNCED,
    SYLLABLE_SYNCED
}

class Lyrics(
    val unsynced: LyricsUnsynced? = null,
    val lineSynced: LyricsLineSynced? = null,
    val syllableSynced: LyricsSyllableSynced? = null
)