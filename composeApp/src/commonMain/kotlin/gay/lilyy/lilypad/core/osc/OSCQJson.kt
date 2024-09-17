package gay.lilyy.lilypad.core.osc

import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.gamestorage.GameStorage
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = AccessSerializer::class)
enum class Access(val int: Int) {
    NO_VALUE(0),
    READ(1),
    WRITE(2),
    READ_WRITE(3)
}
// Serializer for Access enum
object AccessSerializer : KSerializer<Access> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Access", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Access) {
        encoder.encodeInt(value.int)
    }

    override fun deserialize(decoder: Decoder): Access {
        return when (decoder.decodeInt()) {
            0 -> Access.NO_VALUE
            1 -> Access.READ
            2 -> Access.WRITE
            3 -> Access.READ_WRITE
            else -> throw SerializationException("Unknown value")
        }
    }
}

enum class ParameterType {
    INT,
    FLOAT,
    BOOL,
    STRING
}

@Serializable(with = ParameterSerializer::class)
data class Parameter(
    val type: ParameterType,
    val int: Int? = null,
    val float: Float? = null,
    val bool: Boolean? = null,
    val string: String? = null
) {
    override fun toString(): String {
        return when (type) {
            ParameterType.INT -> int.toString()
            ParameterType.FLOAT -> float.toString()
            ParameterType.BOOL -> bool.toString()
            ParameterType.STRING -> string.toString()
        }
    }
    fun any(): Any {
        return when (type) {
            ParameterType.INT -> int!!
            ParameterType.FLOAT -> float!!
            ParameterType.BOOL -> bool!!
            ParameterType.STRING -> string!!
        }
    }
}


object ParameterSerializer : KSerializer<Parameter> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Parameter", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Parameter) {
        when (value.type) {
            ParameterType.INT -> encoder.encodeInt(value.int ?: throw SerializationException("Expected int"))
            ParameterType.BOOL -> encoder.encodeBoolean(value.bool ?: throw SerializationException("Expected bool"))
            ParameterType.FLOAT -> encoder.encodeFloat(value.float ?: throw SerializationException("Expected float"))
            ParameterType.STRING -> encoder.encodeString(value.string ?: throw SerializationException("Expected string"))
        }
    }

    override fun deserialize(decoder: Decoder): Parameter {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This class can only be loaded by Json")
        val element = input.decodeJsonElement() as? JsonPrimitive
            ?: return Parameter(type = ParameterType.BOOL, bool = false)

        return when {
            element.booleanOrNull != null -> Parameter(type = ParameterType.BOOL, bool = element.boolean)
            element.intOrNull != null -> Parameter(type = ParameterType.INT, int = element.int)
            element.floatOrNull != null -> Parameter(type = ParameterType.FLOAT, float = element.float)
            element.contentOrNull != null -> Parameter(type = ParameterType.STRING, string = element.content)
            else -> throw SerializationException("Expected boolean, string, int, or float, found ${element.content}")
        }
    }
}

@Serializable
data class ParameterNode(
    @SerialName("FULL_PATH")
    val fullPath: String,
    @SerialName("ACCESS")
    val access: Access,
    @SerialName("CONTENTS")
    val contents: Map<String, ParameterNode>? = null,
    @SerialName("DESCRIPTION")
    val description: String? = null,
    @SerialName("VALUE")
    val value: List<Parameter>? = null,
    @SerialName("TYPE")
    val type: String? = null
)

object OSCQJson {
    private val client: HttpClient = HttpClient(CIO)
    suspend fun getNode(node: String = "/"): ParameterNode? {
        val gs = Modules.get<GameStorage>("GameStorage")
        if (gs?.oscqPort?.value == null) {
            if (Modules.Core.config!!.logs.errors) Napier.e("GameStorage is null or oscq port not found. Is the game running?")
            return null
        }
        val response = client.get("http://127.0.0.1:${gs.oscqPort.value}$node")
        if (response.status == HttpStatusCode.OK) {
            return Json.decodeFromString(response.bodyAsText())
        } else {
            if (Modules.Core.config!!.logs.errors) Napier.e("Failed to get node $node: ${response.status}")
            return null
        }
    }
}