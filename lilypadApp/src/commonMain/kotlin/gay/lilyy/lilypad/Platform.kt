package gay.lilyy.lilypad

import gay.lilyy.lilypad.core.modules.Module

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openUrlInBrowser(url: String)
expect fun getFilesDir(): String
data class PlatformModuleList(
    val coreModules: List<Module<*>> = listOf(),
    val modules: List<Module<*>> = listOf(),
)
expect fun getPlatformModuleList(): PlatformModuleList