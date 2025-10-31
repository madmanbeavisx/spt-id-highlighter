package com.madmanbeavis.sptidHighlighter.services.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer for Double that accepts strings and handles null/unknown values.
 * Converts string representations to doubles and treats "unknown" or empty strings as null.
 */
class FlexibleDoubleDeserializer : JsonDeserializer<Double> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Double? {
        return try {
            when {
                json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asDouble
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    val str = json.asString
                    if (str.lowercase() == "unknown" || str.isEmpty()) {
                        null
                    } else {
                        str.toDoubleOrNull()
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
