package gay.lilyy.lilypad.core.osc

import com.illposed.osc.MessageSelector
import com.illposed.osc.OSCMessageEvent
import com.illposed.osc.OSCMessageListener
import com.illposed.osc.transport.OSCPortIn
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.core.Core

object OSCReceiver {
    var receiver: OSCPortIn? = null

    val listeners: MutableList<Pair<MessageSelector, OSCMessageListener>> = mutableListOf()
    fun updateAddress() {
        println("Updating OSC receiver address to ${(Modules.modules["Core"] as Core).config!!.listen}")
        receiver?.close()
        receiver = OSCPortIn((Modules.modules["Core"] as Core).config!!.listen)
        for ((selector, listener) in listeners) {
            receiver?.dispatcher?.addListener(selector, listener)
        }
        receiver?.startListening()
    }

    init {
        updateAddress()
    }

    fun addListener(matches: (OSCMessageEvent) -> Boolean, listener: OSCMessageListener) {
        val selector = object : MessageSelector {
            override fun isInfoRequired(): Boolean = false

            override fun matches(event: OSCMessageEvent): Boolean {
                return matches(event)
            }
        }
        listeners += selector to listener
        receiver?.dispatcher?.addListener(selector, listener)
    }
}
