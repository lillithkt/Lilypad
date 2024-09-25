package gay.lilyy.lilypad.core.osc

import com.illposed.osc.MessageSelector
import com.illposed.osc.OSCMessageEvent
import com.illposed.osc.OSCMessageListener
import com.illposed.osc.transport.OSCPortIn
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier

object OSCReceiver {
    var receiver: OSCPortIn? = null

    val listeners: MutableList<Pair<MessageSelector, OSCMessageListener>> = mutableListOf()
    fun updateAddress() {
        if (CoreModules.Core.config!!.logs.debug) Napier.d("Updating OSC receiver address to ${CoreModules.Core.config!!.listen}")
        receiver?.close()
        receiver = OSCPortIn(CoreModules.Core.config!!.listen)
        for ((selector, listener) in listeners) {
            Napier.d("Add OSC listener ${selector} ${listener}")
            receiver?.dispatcher?.addListener(selector, listener)
        }
        receiver?.startListening()
    }

    init {
        updateAddress()
        addListener({ true }) {
            if (CoreModules.Core.config!!.logs.incomingData)
                Napier.d("<= OSC Received message: ${it.message.address} ${it.message.arguments.joinToString()}")
        }
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
