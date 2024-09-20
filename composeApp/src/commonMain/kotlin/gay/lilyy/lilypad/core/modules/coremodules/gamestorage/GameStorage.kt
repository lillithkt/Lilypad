package gay.lilyy.lilypad.core.CoreModules.Coremodules.gamestorage

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.Module
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.osc.OSCQJson
import gay.lilyy.lilypad.getPlatform
import io.github.aakira.napier.Napier
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.irgaly.kfswatch.KfsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
@Suppress("Unused")
class GameStorage : Module<Any>() {
    override val name = "GameStorage"

    var vrcInstalled: Boolean = false
    // C:\Users\lillith\AppData\LocalLow\VRChat\VRChat
    var vrcAppdataPath: File = File(System.getenv("APPDATA"), "../LocalLow/VRChat/VRChat")

    var curLogFile: File? = null
    var curLogFileLineNum: Int = 0
    var curUsername: MutableState<String?> = mutableStateOf(null)
    var curUserId: MutableState<String?> = mutableStateOf(null)
    var curAvatarId: MutableState<String?> = mutableStateOf(null)
    var oscqPort: MutableState<Int?> = mutableStateOf(null)

    private fun parseLine(line: String) {
        // enum class LogEvents(matcher: Regex) {
        for (event in LogEvents.entries) {
            val match = event.matcher.find(line)
            if (match != null)
                event.parse(match)
        }
    }

    private fun parseBackLogs() {
        // read each line and pipe to parseLine
        curLogFileLineNum = 0
        curLogFile?.forEachLine {
            parseLine(it)
            curLogFileLineNum++
        }
    }

    private fun updateLogFile() {
        if (!vrcInstalled) return
        val logs = vrcAppdataPath.listFiles { file ->
            file.name.endsWith(".txt") && file.name.startsWith("output_log_")
        }

        val last = logs.sorted().last()
        val diffFile = curLogFile?.name !== last.name
        curLogFile = last
        if (diffFile) {
            parseBackLogs()
        }
    }

    private suspend fun watchLogs(watcher: KfsDirectoryWatcher) {
        watcher.add(vrcAppdataPath.toString())
        watcher.onEventFlow.collect { event ->
            when (event.event) {
                KfsEvent.Create -> {
                    if (event.path.startsWith("output_log_") && event.path.endsWith(".txt")) {
                        updateLogFile()
                    }
                }
                KfsEvent.Modify -> {
                    if (event.path == curLogFile?.name) {
                        var i = 0
                        curLogFile?.forEachLine {
                            if (i >= curLogFileLineNum) {
                                parseLine(it)
                                curLogFileLineNum = i
                            }
                            i++
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private suspend fun getCurAvatarId(): String? {
        return OSCQJson.getNode("/avatar/change")?.let {
            curAvatarId.value = it.value?.first()?.string
            curAvatarId.value
        }
    }

    override fun init() {
        super.init()
        val scope = CoroutineScope(Dispatchers.IO)
        if (!getPlatform().name.contains("Java")) {
            if (CoreModules.Core.config!!.logs.debug) Napier.v("Running on android, log scanning disabled")
        } else {
            if (vrcAppdataPath.exists()) {
                vrcInstalled = true
                updateLogFile()

                val watcher = KfsDirectoryWatcher(scope)
                scope.launch {
                    watchLogs(watcher)
                }
            }


        }

        scope.launch {
            while (true) {
                getCurAvatarId()
                delay(5000)
            }
        }
    }
}