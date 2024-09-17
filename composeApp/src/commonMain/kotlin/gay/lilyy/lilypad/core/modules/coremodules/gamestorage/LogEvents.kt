package gay.lilyy.lilypad.core.modules.coremodules.gamestorage

import gay.lilyy.lilypad.core.modules.Modules
import io.github.aakira.napier.Napier

enum class LogEvents(val matcher: Regex, val parse: (MatchResult) -> Unit) {
    USER_LOGIN(Regex("User Authenticated: (.*) \\((usr_.*)\\)"), {
        val (username, userId) = it.destructured
        val gs = Modules.get<GameStorage>("GameStorage")!!
        gs.curUsername.value = username
        gs.curUserId.value = userId
        if (Modules.Core.config!!.logs.debug) Napier.d("Found user $username with id $userId")
    }),
    INITIAL_AVATAR_LOAD(Regex("- avatar: |Loading Avatar Data:(avtr_.*)"), {
        val (avatarId) = it.destructured
        val gs = Modules.get<GameStorage>("GameStorage")!!
        gs.curAvatarId.value = avatarId
        if (Modules.Core.config!!.logs.debug) Napier.d("Found avatar $avatarId")
    }),

    OSCQ_PORT(Regex("\\[Always] Advertising Service VRChat-Client-[A-Z0-9]+ of type OSCQuery on (\\d+)"), {
        val (port) = it.destructured
        val gs = Modules.get<GameStorage>("GameStorage")!!
        gs.oscqPort.value = port.toInt()
        if (Modules.Core.config!!.logs.debug) Napier.d("Found OSCQuery port $port")
    }),

    APPLICATION_QUIT(Regex("\\[Player] OnApplicationQuit"), {
        val gs = Modules.get<GameStorage>("GameStorage")!!
        gs.curUsername.value = null
        gs.curUserId.value = null
        gs.curAvatarId.value = null
        gs.oscqPort.value = null
        if (Modules.Core.config!!.logs.debug) Napier.d("VRChat quit")
    })
}