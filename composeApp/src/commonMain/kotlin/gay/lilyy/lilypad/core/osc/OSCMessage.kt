package gay.lilyy.lilypad.core.osc

import com.illposed.osc.OSCMessage


fun OSCMessage.toFormattedString(): String {
    return "OSCMessage(${address} ${arguments.joinToString(" ")})"
}