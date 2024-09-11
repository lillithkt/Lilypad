package gay.lilyy.lilypad.core.modules.modules.spotify

import gay.lilyy.lilypad.core.modules.coremodules.chatbox.ChatboxModule

@Suppress("unused")
class Spotify : ChatboxModule<SpotifyConfig>() {
    override val name = "Spotify"

    override val configClass = SpotifyConfig::class

    override fun buildChatbox(): List<String> {
        return listOf("test")
    }

    init {
        init()
    }
}