package com.madmanbeavis.sptidHighlighter.services.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer for Int that accepts strings and handles null/unknown values.
 * Converts string representations to integers and treats "unknown" or empty strings as null.
 */
class FlexibleIntDeserializer : JsonDeserializer<Int> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Int? {
        return try {
            when {
                json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asInt
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    val str = json.asString
                    if (str.lowercase() == "unknown" || str.isEmpty()) {
                        null
                    } else {
                        str.toIntOrNull()
                    }
                }
                json.isJsonNull -> null
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
