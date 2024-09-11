package gay.lilyy.lilypad.core.storage

import gay.lilyy.lilypad.core.osc.OSCReceiver

//object ParamsStorage {
//    val avatars = mutableMapOf<String, MutableMap<String, List<Any>>>()
//    init {
//        OSCReceiver.addListener({
//            it.message.address.startsWith("/avatar/parameters")
//        }, { message ->
//            println("Received parameters: ${params.joinToString()}")
//        })
//    }
//}