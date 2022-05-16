package moe.sdl.analyzer.qqdb.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

typealias MessageChain = List<TextMessage>

val json = Json {
  ignoreUnknownKeys = true
}

/**
 * 只需要解析文本, 尽可能简单
 */
@Serializable
data class TextMessage(
  @SerialName("msg-type") val type: Int? = null,
  @SerialName("text") val text: String? = null,
)
