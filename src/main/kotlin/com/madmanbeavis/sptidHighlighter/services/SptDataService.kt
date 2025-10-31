package com.madmanbeavis.sptidHighlighter.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.madmanbeavis.sptidHighlighter.models.ItemDetails
import com.madmanbeavis.sptidHighlighter.settings.SptIdSettingsState
import java.io.InputStreamReader

class SptDataService {
    private val gson = Gson()
    private val logger = Logger.getInstance(SptDataService::class.java)
    private var itemsCache: MutableMap<String, ItemDetails> = mutableMapOf()
    private var translationsCache: Map<String, String> = emptyMap()
    private var customItems: Map<String, ItemDetails> = emptyMap()

    companion object {
        fun getInstance(): SptDataService {
            return ApplicationManager.getApplication().getService(SptDataService::class.java)
        }
    }

    init {
        loadData()
    }

    fun loadData() {
        val settings = SptIdSettingsState.getInstance()
        val language = settings.language

        // Load base data
        itemsCache = loadItemsData(language)

        // Load translations
        translationsCache = loadTranslations(language)

        logger.info("SPT Data Service initialized with language: $language")
    }

    private fun loadItemsData(language: String): MutableMap<String, ItemDetails> {
        return try {
            val resourcePath = "/database/$language.json"
            val inputStream = javaClass.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Language file not found: $resourcePath")

            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, ItemDetails>>() {}.type
            val data: Map<String, ItemDetails> = gson.fromJson(reader, type)
            reader.close()

            data.toMutableMap()
        } catch (e: Exception) {
            logger.error("Failed to load items data for language: $language", e)
            // Fallback to English
            if (language != "en") {
                loadItemsData("en")
            } else {
                mutableMapOf()
            }
        }
    }

    private fun loadTranslations(language: String): Map<String, String> {
        return try {
            val resourcePath = "/translations/$language.json"
            val inputStream = javaClass.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Translation file not found: $resourcePath")

            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, String>>() {}.type
            val data: Map<String, String> = gson.fromJson(reader, type)
            reader.close()

            data
        } catch (e: Exception) {
            logger.error("Failed to load translations for language: $language", e)
            // Fallback to English
            if (language != "en") {
                loadTranslations("en")
            } else {
                emptyMap()
            }
        }
    }

    fun getItemDetails(id: String): ItemDetails? {
        // Check custom items first
        customItems[id]?.let { return it }
        // Then check base items
        return itemsCache[id]
    }

    fun getAllItemIds(): Set<String> {
        return (itemsCache.keys + customItems.keys).toSet()
    }

    fun getTranslation(key: String): String {
        return translationsCache[key] ?: key
    }

    fun setCustomItems(items: Map<String, ItemDetails>) {
        customItems = items
        logger.info("Loaded ${items.size} custom items from .sptids file")
    }

    fun clearCustomItems() {
        customItems = emptyMap()
        logger.info("Cleared custom items")
    }
}
