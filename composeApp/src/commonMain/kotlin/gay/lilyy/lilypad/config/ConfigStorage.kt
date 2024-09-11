package gay.lilyy.lilypad.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

@Serializable
data class GlobalConfig(
    val core: CoreConfig = CoreConfig(),
    val modules: ModulesConfig = ModulesConfig()
)

object ConfigStorage {
    val jsonEncoder = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private val configFile = File("config/config.json")

    val all: GlobalConfig

    init {
        // Create config dirs if don't exist
        val configDir = File("config")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        println("Loading config from ${configFile.absolutePath}")

        all = if (configFile.exists()) {
            try {
                jsonEncoder.decodeFromString<GlobalConfig>(configFile.readText())
            } catch (e: Exception) {
                println("Failed loading ${configFile.absolutePath}: ${e.message}")
                GlobalConfig()
            }
        } else {
            GlobalConfig()
        }
    }


    fun save() {
        println("Saving config to ${configFile.absolutePath}")
        configFile.writeText(jsonEncoder.encodeToString(all))
    }
}