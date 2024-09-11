package gay.lilyy.lilypad.core.modules.coremodules.chatbox

class Chatbox : ChatboxModule() {
    override val name = "Chatbox"

    override val chatboxBuildOrder = 0

    override fun onChatboxBuild(): String? {
        return null
    }
}