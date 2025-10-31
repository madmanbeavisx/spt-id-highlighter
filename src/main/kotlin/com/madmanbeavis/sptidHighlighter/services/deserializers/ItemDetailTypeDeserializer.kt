package com.madmanbeavis.sptidHighlighter.services.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.madmanbeavis.sptidHighlighter.models.ItemDetailType
import java.lang.reflect.Type

/**
 * Custom deserializer for ItemDetailType that accepts strings and converts them to enum values.
 * Handles case-insensitive conversion and defaults to ITEM for invalid values.
 */
class ItemDetailTypeDeserializer : JsonDeserializer<ItemDetailType> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemDetailType? {
        return try {
            val value = json.asString.uppercase()
            ItemDetailType.valueOf(value)
        } catch (_: Exception) {
            // If it fails, default to ITEM
            ItemDetailType.ITEM
        }
    }
}
