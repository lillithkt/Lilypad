package gay.lilyy.lilypad

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openUrlInBrowser(url: String)
expect fun getFilesDir(): String