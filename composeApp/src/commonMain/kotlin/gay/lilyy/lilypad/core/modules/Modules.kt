package gay.lilyy.lilypad.core.modules

import androidx.compose.runtime.Composable
import gay.lilyy.lilypad.config.ConfigStorage
import org.reflections.Reflections
import kotlin.reflect.KClass


abstract class Module {
    abstract val name: String

    // --- Config ---

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
    val modules: MutableList<Module> = mutableListOf()

    fun registerModules() {
        val modulePackages = listOf("gay.lilyy.lilypad.core.modules.coremodules") + ConfigStorage.all.core.modulePackages
        val reflections = modulePackages.map { Reflections(it) }.reduce { acc, reflections -> acc.merge(reflections) }
        val moduleClasses = reflections.getSubTypesOf(Module::class.java)

        for (moduleClass in moduleClasses) {
            try {
                // Check if the module is abstract
                if (moduleClass.kotlin.isAbstract) {
                    continue
                }
                val moduleInstance = moduleClass.getDeclaredConstructor().newInstance() as Module
                println("Registering module ${moduleInstance.name}")
                modules.add(moduleInstance)
            } catch (e: Exception) {
                println("Failed to register module ${moduleClass.simpleName}")
                e.printStackTrace()
            }
        }
    }

    init {
        registerModules()
    }
}
