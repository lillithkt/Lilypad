package gay.lilyy.lilypad.core

import java.net.NetworkInterface

object Utils {
    fun getLocalIp(): String =
        NetworkInterface.getNetworkInterfaces().asSequence().first { netInt ->
            netInt.isUp && !netInt.isLoopback && !netInt.isVirtual && netInt.interfaceAddresses.any { it.address.isSiteLocalAddress && it.broadcast != null }
        }.interfaceAddresses.first {
            it.address.isSiteLocalAddress && it.broadcast != null
        }.address.hostAddress
}