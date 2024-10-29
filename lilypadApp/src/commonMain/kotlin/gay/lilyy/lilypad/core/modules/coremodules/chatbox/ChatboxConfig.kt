package gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ChatboxConfig(
    var enabled: Boolean = true,
    val updateInterval: Int = 2000,

    @SerialName("Trim Explanation")
    val _trimExplanation: String = "You are limited to 9 lines and 144 characters total. trimByModule will not add that module to the chatbox if it pushes you over the limit. trimByLine will not add any extra lines that go over the limit, but will include the rest of the module",
    val trimByModule: Boolean = true,
    val trimByLine: Boolean = false,
    var transparent: Boolean = false,
)
