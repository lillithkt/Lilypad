package gay.lilyy.lilypad.core.modules.coremodules.chatbox

@Suppress("unused")
class Chatbox : ChatboxModule<ChatboxConfig>() {
    override val name = "Chatbox"

    override val configClass = ChatboxConfig::class

    override val chatboxBuildOrder = 0

    override fun onChatboxBuild(): String? {
        return null
    }

    init {
        init()
    }
}