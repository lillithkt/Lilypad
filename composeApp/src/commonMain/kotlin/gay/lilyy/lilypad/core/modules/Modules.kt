package gay.lilyy.lilypad.core.modules

import androidx.compose.runtime.Composable
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.Chatbox
import gay.lilyy.lilypad.core.CoreModules.CoreCoreModules.Core.Core
import gay.lilyy.lilypad.core.CoreModules.Coremodules.gamestorage.GameStorage
import io.github.aakira.napier.Napier
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.reflections.Reflections
import java.io.File
import java.util.*
import kotlin.reflect.KClass

@OptIn(kotlinx. serialization. InternalSerializationApi::class)
abstract class Module<T : Any> {
    abstract val name: String

    open val disabled: Boolean = false

    // --- Config ---
    open var configName: String? = null
    open val configClass: KClass<T>? = null

    var config: T? = null

    open fun init() {
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
            if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to load config for module $name", e)
        }
    }

    open fun saveConfig(write: Boolean = true) {
        if (config != null) {
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

    // --- HTTP Server ---
    open fun httpServerRouting(routing: Routing): Boolean {
        return false
    }
}

object CoreModules {
    val Core: Core = Core()
    val GameStorage: GameStorage = GameStorage()
    val Chatbox: Chatbox = Chatbox()
    
    val all: List<Module<*>> = listOf(
        Core,
        GameStorage,
        Chatbox
    )

    init {
        for (module in all) {
            try {
                Modules.modules[module.name] = module
                module.init()
            } catch (e: Exception) {
                if (Core.config!!.logs.errors) Napier.e("Failed to init Core Module ${module.name}", e)
            }
        }
    }
}

object Modules {
    val modules: MutableMap<String, Module<*>> = mutableMapOf("Core" to Core())
    

    inline fun <reified T : Module<*>> get(name: String): T? {
        return modules[name] as T?
    }

    private fun registerModulesFromPackage(packageName: String) {
        val reflections = Reflections(packageName)
        val moduleClasses = reflections.getSubTypesOf(Module::class.java)

        for (moduleClass in moduleClasses) {
            try {
                // Check if the module is abstract
                if (moduleClass.kotlin.isAbstract || moduleClass === Core::class.java) {
                    continue
                }
                val moduleInstance = moduleClass.getDeclaredConstructor().newInstance() as Module
                if (!moduleInstance.disabled) {
                    if (CoreModules.Core.config!!.logs.debug) Napier.v("Registering module ${moduleInstance.name}")
                    moduleInstance.init()
                    modules[moduleInstance.name] = moduleInstance
                } else {
                    if (CoreModules.Core.config!!.logs.debug) Napier.v("Skipping disabled module ${moduleInstance.name}")
                }
            } catch (e: Exception) {
                if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to register module ${moduleClass.simpleName}", e)
            }
        }
    }

    private fun registerModules() {
        for (packageName in CoreModules.Core.config!!.modulePackages) {
            registerModulesFromPackage(packageName)
        }
    }

    init {
        CoreModules.Core.init()
        registerModules()
    }
}

object ConfigStorage {
    val jsonEncoder = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val configFile = File("config.json")

    var all: MutableMap<String, JsonElement> = mutableMapOf()

    init {
        try {
            if (CoreModules.Core.config?.logs?.debug == true) Napier.v("Loading config from ${configFile.absolutePath}")
        } catch(e: Exception) {
            /* no-op */
        }
        all = if (configFile.exists()) {
            try {
                jsonEncoder.decodeFromString<MutableMap<String, JsonElement>>(configFile.readText())
            } catch (e: Exception) {
                if (CoreModules.Core.config?.logs?.errors == true) Napier.e("Failed loading ${configFile.absolutePath}: ${e.message}", e)
                mutableMapOf()
            }
        } else {
            mutableMapOf()
        }
    }

    fun save() {
        if (CoreModules.Core.config?.logs?.debug == true) Napier.v("Saving config to ${configFile.absolutePath}")
        configFile.writeText(jsonEncoder.encodeToString(all))
    }
}