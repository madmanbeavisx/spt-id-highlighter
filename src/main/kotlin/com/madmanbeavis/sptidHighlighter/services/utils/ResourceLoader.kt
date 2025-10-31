package com.madmanbeavis.sptidHighlighter.services.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import java.io.InputStreamReader

/**
 * Utility for loading resources from the classpath.
 */
object ResourceLoader {

    @PublishedApi
    internal val logger = Logger.getInstance(ResourceLoader::class.java)

    @PublishedApi
    internal val gson = Gson()
    
    /**
     * Loads a JSON resource file and deserializes it to the specified type.
     * Returns null if the resource cannot be loaded.
     */
    inline fun <reified T> loadJsonResource(resourcePath: String): T? {
        return try {
            val inputStream = ResourceLoader::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")
            
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<T>() {}.type
            val data: T = gson.fromJson(reader, type)
            reader.close()
            
            data
        } catch (e: Exception) {
            logger.error("Failed to load resource: $resourcePath", e)
            null
        }
    }
    
    /**
     * Checks if a resource exists at the given path.
     */
    fun resourceExists(resourcePath: String): Boolean {
        return try {
            ResourceLoader::class.java.getResourceAsStream(resourcePath) != null
        } catch (e: Exception) {
            false
        }
    }
}
