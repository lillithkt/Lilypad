package gay.lilyy.lilypad.core.modules

import androidx.compose.runtime.Composable
import gay.lilyy.lilypad.core.modules.coremodules.core.Core
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.reflections.Reflections
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.system.exitProcess

@OptIn(kotlinx. serialization. InternalSerializationApi::class)
abstract class Module<T : Any> {
    abstract val name: String

    // --- Config ---
    open var configName: String? = null
    open val configClass: KClass<T>? = null

    var config: T? = null

    fun init() {
        configName = configName ?: name.replace(" ", "").lowercase(Locale.getDefault())
        loadConfig()
    }

    open fun loadConfig() {
        try {
            if (configClass != null) {
                val jsonObject = ConfigStorage.all[configName]
                config = if (jsonObject != null) {
                    ConfigStorage.jsonEncoder.decodeFromJsonElement(configClass!!.serializer(), jsonObject)
                } else {
                    configClass?.java?.getDeclaredConstructor()?.newInstance()
                }
            }
        } catch (e: Exception) {
            println("Failed to load config for module $name")
            e.printStackTrace()
            config = configClass?.java?.getDeclaredConstructor()?.newInstance()
        }
    }

    open fun saveConfig(write: Boolean = true) {
        if (config != null) {
            println("Saving config for module $name")
            ConfigStorage.all[configName!!] = ConfigStorage.jsonEncoder.encodeToJsonElement(configClass!!.serializer(), config!!)
            if (write) {
                ConfigStorage.save()
            }
        }
    }
    /**
     * Whether this module has a settings UI.
     */
    open val hasSettingsUI = false

    /**
     * Called to build the settings UI.
     */
    @Composable
    open fun onSettingsUI() {}
}
object Modules {
    val modules: MutableMap<String, Module<*>> = mutableMapOf()

    private fun registerModulesFromPackage(packageName: String) {
        val reflections = Reflections(packageName)
        val moduleClasses = reflections.getSubTypesOf(Module::class.java)

        for (moduleClass in moduleClasses) {
            try {
                // Check if the module is abstract
                if (moduleClass.kotlin.isAbstract) {
                    continue
                }
                val moduleInstance = moduleClass.getDeclaredConstructor().newInstance() as Module
                println("Registering module ${moduleInstance.name}")
                modules[moduleInstance.name] = moduleInstance
            } catch (e: Exception) {
                println("Failed to register module ${moduleClass.simpleName}")
                e.printStackTrace()
            }
        }
    }

    private fun registerModules() {
        registerModulesFromPackage("gay.lilyy.lilypad.core.modules.coremodules")
        if (modules["Core"] == null) {
            println("Core module not found")
            exitProcess(1)
        } else {
            val coreModule = modules["Core"] as Core
            for (packageName in coreModule.config!!.modulePackages) {
                registerModulesFromPackage(packageName)
            }
        }
    }

    init {
        registerModules()
    }
}

object ConfigStorage {
    val jsonEncoder = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private val configFile = File("config/config.json")

    var all: MutableMap<String, JsonElement> = mutableMapOf()

    init {
        println("Loading config from ${configFile.absolutePath}")

        all = if (configFile.exists()) {
            try {
                jsonEncoder.decodeFromString<MutableMap<String, JsonElement>>(configFile.readText())
            } catch (e: Exception) {
                println("Failed loading ${configFile.absolutePath}: ${e.message}")
                mutableMapOf()
            }
        } else {
            mutableMapOf()
        }
    }

    fun save() {
        println("Saving config to ${configFile.absolutePath}")
        configFile.writeText(jsonEncoder.encodeToString(all))
    }
}