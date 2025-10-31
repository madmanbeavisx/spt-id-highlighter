package com.madmanbeavis.sptidHighlighter.services.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer for Boolean that accepts "Unknown" as null and handles various string representations.
 * Supports: true/false, yes/no, 1/0, unknown (as null), and empty string (as null).
 */
class FlexibleBooleanDeserializer : JsonDeserializer<Boolean> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Boolean? {
        return try {
            when {
                json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    val str = json.asString.lowercase()
                    when (str) {
                        "true", "yes", "1" -> true
                        "false", "no", "0" -> false
                        "unknown", "" -> null
                        else -> null
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
